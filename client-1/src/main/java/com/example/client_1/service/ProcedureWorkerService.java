package com.example.client_1.service;

import com.example.common.ProcedureExecutor;
import com.example.common.exception.ProcedureExecutionException;
import com.example.common.model.ProcedureRequest;
import com.example.common.model.ProcedureResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Service;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Optional;

import static com.example.common.exception.ExceptionMessages.PROCEDURE_NOT_FOUND;


@Service
@Slf4j
@RequiredArgsConstructor
public class ProcedureWorkerService {

    final Map<String, ProcedureExecutor<?, ?>> procedures;
    final ObjectMapper objectMapper;

    @KafkaListener(topics = "client-1-procedures", groupId = "worker-client-1")
    @SendTo
    public ProcedureResponse<?> handleClient1Procedure(ProcedureRequest<?> request) {
        log.info("Processing CLIENT-1 procedure: {} (requestId: {})", request.procedureName(), request.requestId());
        log.info("Received parameters: {}", request.parameters());

        return executeProcedure(request);
    }

    /**
     * Выполняет процедуру и возвращает результат
     */
    public <P, R> ProcedureResponse<R> executeProcedure(ProcedureRequest<P> request) {
        var procedureName = request.procedureName();

        return Optional.ofNullable(procedures.get(procedureName))
                .map(executor -> {
                    // Получаем тип параметра для конвертации
                    Class<?> parameterType = getParameterType(executor);
                    
                    P convertedParams = (P) objectMapper.convertValue(request.parameters(), parameterType);
                    
                    // Выполняем процедуру с правильными параметрами
                    return ((ProcedureExecutor<P, R>) executor).execute(convertedParams);
                })
                .map(result -> ProcedureResponse.<R>builder()
                        .requestId(request.requestId())
                        .success(true)
                        .result(result)
                        .build())
                .orElseThrow(() -> new ProcedureExecutionException(PROCEDURE_NOT_FOUND.formatted(procedureName)));
    }

    /**
     * Извлекает тип параметра из ProcedureExecutor<P, R>
     */
    Class<?> getParameterType(ProcedureExecutor<?, ?> executor) {
        // Получаем все интерфейсы класса
        Type[] genericInterfaces = executor.getClass().getGenericInterfaces();

        for (Type genericInterface : genericInterfaces) {
            if (genericInterface instanceof ParameterizedType paramType) {
                Type rawType = paramType.getRawType();

                // Проверяем, что это ProcedureExecutor
                if (rawType instanceof Class<?> clazz && ProcedureExecutor.class.isAssignableFrom(clazz)) {
                    // Получаем первый generic-параметр (P в ProcedureExecutor<P, R>)
                    Type[] typeArguments = paramType.getActualTypeArguments();
                    if (typeArguments.length > 0 && typeArguments[0] instanceof Class<?>) {
                        return (Class<?>) typeArguments[0];
                    }
                }
            }
        }

        // Если не удалось определить тип, возвращаем Object
        log.warn("Could not determine parameter type for executor: {}", executor.getClass().getName());
        return Object.class;
    }
}
