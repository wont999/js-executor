package com.example.routing.exception;

/**
 * Константы для сообщений об ошибках
 */
public final class ExceptionMessages {

    // Kafka related messages
    public static final String KAFKA_SEND_FAILED = "Failed to send request %s to topic %s";

    // Timeout related messages
    public static final String REQUEST_TIMEOUT = "Request %s timed out after %d seconds";

    // Procedure execution messages
    public static final String PROCEDURE_EXECUTION_ERROR = "Error executing procedure: %s";
    public static final String REQUEST_INTERRUPTED = "Request was interrupted";

    // Global error messages
    public static final String INTERNAL_SERVER_ERROR = "Internal server error: %s";
    public static final String KAFKA_SERVICE_UNAVAILABLE = "Failed to send request to message broker";
    public static final String TIMEOUT_ERROR = "Request timed out after %d seconds";
}
