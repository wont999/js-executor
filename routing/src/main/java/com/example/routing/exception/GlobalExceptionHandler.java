package com.example.routing.exception;

import com.example.common.model.ProcedureResponse;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Обработка исключений при таймауте запроса
     */
    @ExceptionHandler(RequestTimeoutException.class)
    public ResponseEntity<ProcedureResponse> handleRequestTimeout(
            RequestTimeoutException ex, WebRequest request) {

        log.error("Request timeout: {}", ex.getMessage());

        val response = ProcedureResponse.builder()
                .success(false)
                .errorMessage(ex.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body(response);
    }

    /**
     * Обработка исключений при ошибке выполнения процедуры
     */
    @ExceptionHandler(ProcedureExecutionException.class)
    public ResponseEntity<ProcedureResponse> handleProcedureExecution(
            ProcedureExecutionException ex, WebRequest request) {

        log.error("Procedure execution error: {}", ex.getMessage(), ex);

        val response = ProcedureResponse.builder()
                .success(false)
                .errorMessage(ex.getMessage())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    /**
     * Обработка исключений при ошибке отправки в Kafka
     */
    @ExceptionHandler(KafkaSendException.class)
    public ResponseEntity<ProcedureResponse> handleKafkaSend(
            KafkaSendException ex, WebRequest request) {

        log.error("Kafka send error: {}", ex.getMessage(), ex);

        val response = ProcedureResponse.builder()
                .success(false)
                .errorMessage(ex.getMessage())
                .build();

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }

    /**
     * Обработка исключений когда сервис не найден в Eureka Discovery
     */
    @ExceptionHandler(ServiceNotFoundException.class)
    public ResponseEntity<ProcedureResponse> handleServiceNotFound(
            ServiceNotFoundException ex, WebRequest request) {

        log.error("Service not found: {}", ex.getMessage());

        val response = ProcedureResponse.builder()
                .success(false)
                .errorMessage(ex.getMessage())
                .build();

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }

    /**
     * Обработка исключений при неверных аргументах (например, неизвестный тип клиента)
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ProcedureResponse> handleIllegalArgument(
            IllegalArgumentException ex, WebRequest request) {

        log.error("Invalid argument: {}", ex.getMessage());

        val response = ProcedureResponse.builder()
                .success(false)
                .errorMessage(ex.getMessage())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Обработка всех остальных неожиданных исключений
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProcedureResponse> handleGenericException(
            Exception ex, WebRequest request) {

        log.error("Unexpected error: {}", ex.getMessage(), ex);

        val response = ProcedureResponse.builder()
                .success(false)
                .errorMessage(ex.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
