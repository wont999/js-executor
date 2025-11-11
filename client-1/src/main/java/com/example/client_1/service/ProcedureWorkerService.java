package com.example.client_1.service;

import com.example.common.ProcedureExecutor;
import com.example.common.exception.ProcedureExecutionException;
import com.example.common.mapper.ProcedureMapper;
import com.example.common.model.ProcedurePayload;
import com.example.common.model.ProcedureResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.UUID;

import static com.example.common.exception.ExceptionMessages.PROCEDURE_NOT_FOUND;


@Service
@Slf4j
@RequiredArgsConstructor
public class ProcedureWorkerService {

    final KafkaTemplate<String, ProcedureResponse<?>> kafkaTemplate;
    final Map<String, ProcedureExecutor<?, ?>> procedures;
    final ObjectMapper objectMapper;
    final ProcedureMapper procedureMapper;


    public <P, R> ProcedureResponse<R> executeProcedure(ProcedurePayload<P> request) {
        ProcedureExecutor<P, R> executor = getExecutor(request.procedureName());

        P params = convertParameters(request.parameters(), executor);
        R result = executor.execute(params);

        log.info("Procedure {} completed successfully (requestId: {})", request.procedureName(), request.requestId());

        return procedureMapper.toResponse(request, result);
    }

    <P, R> ProcedureExecutor<P, R> getExecutor(String procedureName) {
        ProcedureExecutor<?, ?> executor = procedures.get(procedureName);

        if (executor == null) {
            throw new ProcedureExecutionException(PROCEDURE_NOT_FOUND.formatted(procedureName));
        }

        return (ProcedureExecutor<P, R>) executor;
    }

    <P> P convertParameters(Object parameters, ProcedureExecutor<P, ?> executor) {
        if (parameters == null) {
            return null;
        }

        Class<P> parameterType = getParameterType(executor);

        if (parameterType.isInstance(parameters)) {
            return (P) parameters;
        }

        P converted = objectMapper.convertValue(parameters, parameterType);
        log.debug("Successfully converted parameters to type: {}", parameterType.getSimpleName());

        return converted;
    }

    <P> Class<P> getParameterType(ProcedureExecutor<P, ?> executor) {
        for (Type genericInterface : executor.getClass().getGenericInterfaces()) {
            if (genericInterface instanceof ParameterizedType paramType
                    && paramType.getRawType().equals(ProcedureExecutor.class)) {
                Type[] typeArguments = paramType.getActualTypeArguments();
                if (typeArguments.length > 0 && typeArguments[0] instanceof Class<?> clazz
                        && clazz != Object.class) {
                    return (Class<P>) clazz;
                }
            }
        }

        log.warn("Could not determine specific parameter type for executor: {}, using Object.class",
                executor.getClass().getName());
        return (Class<P>) Object.class;
    }

    public void sendResponse(String replyToTopic, UUID requestId, ProcedureResponse<?> response) {
        log.info("Sending response for requestId: {} to topic: {}", requestId, replyToTopic);

        kafkaTemplate.send(replyToTopic, requestId.toString(), response)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to send response for requestId: {} to topic: {}: {}",
                                requestId, replyToTopic, ex.getMessage(), ex);
                    } else {
                        log.debug("Response sent successfully for requestId: {} to partition: {}, offset: {}",
                                requestId,
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    }
                });
    }
}