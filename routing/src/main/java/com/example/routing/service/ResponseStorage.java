package com.example.routing.service;

import com.example.common.model.ProcedureResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * In-memory storage для ожидания ответов на запросы.
 * Использует CompletableFuture для блокировки HTTP-запроса до получения ответа из Kafka.
 * Работает только в пределах одного экземпляра сервиса.
 */
@Service
@Slf4j
public class ResponseStorage {

    final ConcurrentHashMap<String, CompletableFuture<ProcedureResponse>> pendingRequests = new ConcurrentHashMap<>();

    /**
     * Создает ожидание для запроса
     */
    public CompletableFuture<ProcedureResponse> createPendingRequest(String requestId) {
        CompletableFuture<ProcedureResponse> future = new CompletableFuture<>();
        pendingRequests.put(requestId, future);
        log.debug("Created pending request: {}", requestId);
        return future;
    }

    /**
     * Завершает ожидание с результатом
     */
    public void completeRequest(String requestId, ProcedureResponse response) {
        CompletableFuture<ProcedureResponse> future = pendingRequests.remove(requestId);
        if (future != null) {
            future.complete(response);
            log.debug("Completed request: {}", requestId);
        } else {
            log.warn("Received response for unknown request: {}", requestId);
        }
    }

    /**
     * Отменяет ожидание по таймауту
     */
    public void timeoutRequest(String requestId) {
        CompletableFuture<ProcedureResponse> future = pendingRequests.remove(requestId);
        if (future != null) {
            future.completeExceptionally(new RuntimeException("Request timeout"));
            log.warn("Request timed out: {}", requestId);
        }
    }
}
