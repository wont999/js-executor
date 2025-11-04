package com.example.client_1.service.procedure;

import com.example.client_1.model.EmptyParameters;
import com.example.client_1.model.HealthCheckResult;
import com.example.common.ProcedureExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


/**
 * Процедура для проверки здоровья сервиса
 */
@Slf4j
@Component("healthCheck")
public class HealthCheckProcedure implements ProcedureExecutor<EmptyParameters, HealthCheckResult> {
    @Override
    public HealthCheckResult execute(EmptyParameters parameters) {
        log.debug("Executing healthCheck");

        long now = System.currentTimeMillis();
        HealthCheckResult result = new HealthCheckResult(
                "OK",
                "worker-service",
                now,
                now // лучше вынести дату старта приложения, но пока — так как в вашей логике
        );

        log.debug("HealthCheck result: {}", result);
        return result;
    }
}