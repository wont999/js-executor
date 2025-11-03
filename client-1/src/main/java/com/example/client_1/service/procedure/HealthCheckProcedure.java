package com.example.client_1.service.procedure;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Процедура для проверки здоровья сервиса
 */
@Slf4j
@Component("healthCheck")
public class HealthCheckProcedure implements ProcedureExecutor {

    @Override
    public Map<String, Object> execute(Map<String, Object> parameters) {
        log.debug("Executing healthCheck");

        Map<String, Object> result = new HashMap<>();
        result.put("status", "OK");
        result.put("service", "worker-service");
        result.put("timestamp", System.currentTimeMillis());
        result.put("uptime", System.currentTimeMillis());

        log.debug("HealthCheck result: {}", result);
        return result;
    }
}
