package com.example.routing.service;

import com.example.common.model.ProcedureResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory storage для ожидания ответов на запросы.
 * Использует CompletableFuture для блокировки HTTP-запроса до получения ответа из Kafka.
 * Работает только в пределах одного экземпляра сервиса.
 */
@Service
@Slf4j
public class ResponseStorage {

    final ConcurrentHashMap<UUID, CompletableFuture<ProcedureResponse<?>>> pendingRequests = new ConcurrentHashMap<>();

    /**
     * Создает ожидание для запроса
     */
    public CompletableFuture<ProcedureResponse<?>> createPendingRequest(UUID requestId) {
        CompletableFuture<ProcedureResponse<?>> future = new CompletableFuture<>();
        pendingRequests.put(requestId, future);
        log.debug("Created pending request: {}", requestId);
        return future;
    }

    /**
     * Завершает ожидание с результатом
     */
    public void completeRequest(ProcedureResponse<?> response) {
        var future = pendingRequests.remove(response.requestId());
        if (future != null) {
            future.complete(response);
            log.debug("Completed request: {}", response.requestId());
        } else {
            log.warn("Received response for unknown request: {}", response.requestId());
        }
    }

    /**
     * Отменяет ожидание по таймауту
     */
    public void timeoutRequest(UUID requestId) {
        var future = pendingRequests.remove(requestId);
        if (future != null) {
            future.completeExceptionally(new RuntimeException("Request timeout"));
            log.warn("Request timed out: {}", requestId);
        }
    }
}