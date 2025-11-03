package com.example.routing.exception;

import lombok.experimental.StandardException;

/**
 * Исключение, возникающее при ошибке отправки сообщения в Kafka
 */
@StandardException
public class KafkaSendException extends RuntimeException {
}
