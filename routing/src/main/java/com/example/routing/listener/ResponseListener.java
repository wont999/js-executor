package com.example.routing.listener;

import com.example.common.model.ProcedureResponse;
import com.example.routing.service.ResponseStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Kafka listener для получения ответов в уникальный топик этого экземпляра.
 * Топик определяется динамически через instanceId.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class ResponseListener {

    final ResponseStorage responseStorage;

    /**
     * Слушает топик с ответами для данного экземпляра.
     * Топик задается через SpEL выражение: #{instanceId}
     */
    @KafkaListener(
            topics = "#{instanceId}",
            containerFactory = "responseKafkaListenerContainerFactory",
            groupId = "#{instanceId}"
    )
    public void handleResponse(ProcedureResponse response) {
        log.info("Received response for requestId: {}", response.requestId());

        // Завершаем ожидание для соответствующего запроса
        responseStorage.completeRequest(response.requestId(), response);
    }
}
