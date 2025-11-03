package com.example.routing.service;

import com.example.common.model.ProcedureRequestDto;
import com.example.routing.exception.ExceptionMessages;
import com.example.routing.exception.KafkaSendException;
import com.example.routing.exception.ProcedureExecutionException;
import com.example.routing.exception.RequestTimeoutException;
import com.example.common.model.ProcedureRequest;
import com.example.common.model.ProcedureResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Основной сервис для маршрутизации процедур.
 * Отправляет запросы в соответствующие топики на основе типа клиента и ждет ответ.
 * Маршрутизация настраивается динамически через ClientTypeTopicMapper.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProcedureGatewayService {
    final KafkaTemplate<String, ProcedureRequest> kafkaTemplate;

    final ResponseStorage responseStorage;
    
    final ClientTypeTopicMapper clientTypeTopicMapper;

    final String instanceId;

    @Value("${gateway.request.timeout:30}")
    private long requestTimeoutSeconds;

    /**
     * Выполняет процедуру: отправляет в Kafka и синхронно ждет ответ
     */
    public ProcedureResponse executeProcedure(ProcedureRequestDto requestDto) {
        String requestId = UUID.randomUUID().toString();

        // Создаем запрос
        ProcedureRequest request = new ProcedureRequest(
                requestId,
                requestDto.clientType(),
                requestDto.procedureName(),
                requestDto.parameters(),
                instanceId
        );

        // Регистрируем ожидание ответа
        CompletableFuture<ProcedureResponse> responseFuture = responseStorage.createPendingRequest(requestId);

        // Получаем топик для данного типа клиента
        // Автоматически проверяет наличие сервиса в Eureka и генерирует имя топика
        String targetTopic = clientTypeTopicMapper.getTopicForClientType(requestDto.clientType());
        log.info("Sending procedure '{}' to topic '{}' with requestId: {}", requestDto.procedureName(), targetTopic, requestId);

        sendToKafka(requestId, targetTopic, request, responseFuture);

        // Синхронно ждем ответ с таймаутом
        return awaitResponse(requestId, responseFuture);
    }

    /**
     * Отправляет запрос в Kafka
     */
    private void sendToKafka(String requestId, String targetTopic, ProcedureRequest request, 
                             CompletableFuture<ProcedureResponse> responseFuture) {
        kafkaTemplate.send(targetTopic, requestId, request)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to send request {}: {}", requestId, ex.getMessage());
                        String message = String.format(ExceptionMessages.KAFKA_SEND_FAILED, requestId, targetTopic);
                        responseFuture.completeExceptionally(new KafkaSendException(message, ex));
                    } else {
                        log.debug("Request {} sent successfully to {}", requestId, targetTopic);
                    }
                });
    }

    /**
     * Ожидает ответ от worker'а с обработкой ошибок
     * Преобразует checked exceptions в unchecked для глобальной обработки
     */
    private ProcedureResponse awaitResponse(String requestId, CompletableFuture<ProcedureResponse> responseFuture) {
        try {
            ProcedureResponse response = responseFuture.get(requestTimeoutSeconds, TimeUnit.SECONDS);
            log.info("Received response for requestId: {}", requestId);
            return response;
        } catch (TimeoutException e) {
            log.error("Request {} timed out after {} seconds", requestId, requestTimeoutSeconds);
            responseStorage.timeoutRequest(requestId);
            String message = String.format(ExceptionMessages.REQUEST_TIMEOUT, requestId, requestTimeoutSeconds);
            throw new RequestTimeoutException(message, e);
        } catch (ExecutionException e) {
            log.error("Error executing request {}: {}", requestId, e.getMessage());
            responseStorage.timeoutRequest(requestId);

            // Если это уже наше исключение KafkaSendException, пробрасываем его
            if (e.getCause() instanceof KafkaSendException) {
                throw (KafkaSendException) e.getCause();
            }
            String message = String.format(ExceptionMessages.PROCEDURE_EXECUTION_ERROR, e.getMessage());
            throw new ProcedureExecutionException(message, e);
        } catch (InterruptedException e) {
            log.error("Request {} was interrupted", requestId);
            responseStorage.timeoutRequest(requestId);
            Thread.currentThread().interrupt();
            throw new ProcedureExecutionException(ExceptionMessages.REQUEST_INTERRUPTED, e);
        }
    }
}
