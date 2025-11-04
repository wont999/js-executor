package com.example.routing.service;

import com.example.common.exception.ExceptionMessages;
import com.example.common.exception.KafkaSendException;
import com.example.common.model.ProcedureRequest;
import com.example.common.model.ProcedureRequestDto;
import com.example.common.model.ProcedureResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.requestreply.RequestReplyFuture;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;
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
    final ReplyingKafkaTemplate<String, ProcedureRequest, ProcedureResponse> replyingKafkaTemplate;

    final ClientTypeTopicMapper clientTypeTopicMapper;

    final String instanceId;

    @Value("${gateway.request.timeout:30}")
    long requestTimeoutSeconds;

    /**
     * Выполняет процедуру: отправляет в Kafka и синхронно ждет ответ
     */
    public ProcedureResponse executeProcedure(ProcedureRequestDto requestDto) {
        String requestId = UUID.randomUUID().toString();

        var request = ProcedureRequest.builder()
                .requestId(requestId)
                .clientType(requestDto.clientType())
                .procedureName(requestDto.procedureName())
                .parameters(requestDto.parameters())
                .replyTo(instanceId)
                .build();

        try {
            // Получаем топик для данного типа клиента
            // Автоматически проверяет наличие сервиса в Eureka и генерирует имя топика
            String targetTopic = clientTypeTopicMapper.getTopicForClientType(requestDto.clientType());
            log.info("Sending procedure '{}' to topic '{}' with requestId: {}", requestDto.procedureName(), targetTopic, requestId);

            // Создаем ProducerRecord с указанием reply-to топика
            ProducerRecord<String, ProcedureRequest> record = new ProducerRecord<>(targetTopic, requestId, request);
            record.headers().add(KafkaHeaders.REPLY_TOPIC, instanceId.getBytes());

            // Отправляем запрос и синхронно ждем ответ
            RequestReplyFuture<String, ProcedureRequest, ProcedureResponse> replyFuture = 
                    replyingKafkaTemplate.sendAndReceive(record, Duration.ofSeconds(requestTimeoutSeconds));

            // Получаем ответ
            ConsumerRecord<String, ProcedureResponse> consumerRecord = replyFuture.get(requestTimeoutSeconds, TimeUnit.SECONDS);
            ProcedureResponse response = consumerRecord.value();
            
            log.info("Received response for requestId: {}", requestId);
            return response;

        } catch (ExecutionException e) {
            log.error("Failed to execute procedure for requestId {}: {}", requestId, e.getMessage());
            Throwable cause = e.getCause();
            if (cause instanceof KafkaSendException) {
                throw (KafkaSendException) cause;
            }
            String message = String.format(ExceptionMessages.KAFKA_SEND_FAILED, requestId, requestDto.clientType());
            throw new KafkaSendException(message, cause);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Request {} was interrupted", requestId);
            throw new KafkaSendException("Request was interrupted", e);
        } catch (TimeoutException e) {
            log.error("Request {} timed out after {} seconds", requestId, requestTimeoutSeconds);
            throw new KafkaSendException("Request timeout", e);
        }
    }//TODO
}
