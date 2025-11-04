package com.example.common.exception;

/**
 * Константы для сообщений об ошибках
 */
public final class ExceptionMessages {

    // Kafka related messages
    public static final String KAFKA_SEND_FAILED = "Failed to send request %s to topic %s";

    public static final String SERVICE_NOT_FOUND = "Service '%s' not found";

    public static final String TIMEOUT_GENERIC = "Request timed out";

    public static final String PROCEDURE_NOT_FOUND = "Procedure '%s' not found";

    public static final String INVALID_ARGUMENT = "Invalid argument: %s";
}
