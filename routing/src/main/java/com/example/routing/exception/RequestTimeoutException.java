package com.example.routing.exception;

import lombok.experimental.StandardException;

/**
 * Исключение, возникающее при таймауте запроса
 */
@StandardException
public class RequestTimeoutException extends RuntimeException {
}
