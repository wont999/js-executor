package com.example.client_1.service;

import com.example.client_1.service.procedure.ProcedureExecutor;
import com.example.common.model.ProcedureRequest;
import com.example.common.model.ProcedureResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Worker Service для обработки процедур из Kafka топиков.
 * Обрабатывает запросы и отправляет ответы в reply-to топик Gateway.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ProcedureWorkerService {

    final KafkaTemplate<String, ProcedureResponse> kafkaTemplate;
    final Map<String, ProcedureExecutor> procedures;

    /**
     * Обрабатывает процедуры для CLIENT_1
     */
    @KafkaListener(topics = "client-1-procedures", groupId = "worker-client-1")
    public void handleClient1Procedure(ProcedureRequest request) {
        log.info("Processing CLIENT_1 procedure: {} (requestId: {})",
                request.procedureName(), request.requestId());

        ProcedureResponse response = executeProcedure(request);
        sendResponse(request, response);
    }

    /**
     * Выполняет процедуру и возвращает результат
     */
    private ProcedureResponse executeProcedure(ProcedureRequest request) {

        String procedureName = request.procedureName();
        ProcedureExecutor executor = procedures.get(procedureName);

        if (executor == null) {
            throw new IllegalArgumentException("Unknown procedure: " + procedureName);
        }

        Map<String, Object> result = executor.execute(request.parameters());

        var response = ProcedureResponse.builder()
                .requestId(request.requestId())
                .success(true).result(result).build();

        log.info("Procedure {} completed successfully", procedureName);
        return response;

    }

    /**
     * Отправляет ответ в reply-to топик Gateway
     */
    private void sendResponse(ProcedureRequest request, ProcedureResponse response) {
        String replyToTopic = request.replyTo();

        log.info("Sending response for {} to topic: {}", request.requestId(), replyToTopic);

        kafkaTemplate.send(replyToTopic, request.requestId(), response)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to send response for {}: {}",
                                request.requestId(), ex.getMessage());
                    } else {
                        log.debug("Response sent successfully for {}", request.requestId());
                    }
                });
    }
}
