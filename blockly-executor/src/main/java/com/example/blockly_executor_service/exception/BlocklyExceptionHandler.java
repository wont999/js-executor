package com.example.blockly_executor_service.exception;

import com.example.common.model.ProcedureResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.script.ScriptException;

/**
 * Обработчик исключений специфичных для Blockly Executor.
 * Дополняет GlobalExceptionHandler из common пакета.
 * 
 * Примечание: ProcedureExecutionException обрабатывается в GlobalExceptionHandler
 */
@RestControllerAdvice
@Slf4j
public class BlocklyExceptionHandler {

    /**
     * Обработка ошибок компиляции/выполнения JavaScript
     */
    @ExceptionHandler(ScriptException.class)
    public ResponseEntity<ProcedureResponse<?>> handleScriptException(ScriptException ex) {
        log.error("JavaScript execution error: {}", ex.getMessage(), ex);

        ProcedureResponse<?> response = ProcedureResponse.builder()
                .success(false)
                .errorMessage("Script error: " + ex.getMessage())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Обработка ошибок безопасности (например, отсутствие tenantId)
     */
    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<ProcedureResponse<?>> handleSecurityException(SecurityException ex) {
        log.error("Security error: {}", ex.getMessage(), ex);

        ProcedureResponse<?> response = ProcedureResponse.builder()
                .success(false)
                .errorMessage("Security error: " + ex.getMessage())
                .build();

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }
}