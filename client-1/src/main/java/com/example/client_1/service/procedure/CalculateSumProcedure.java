package com.example.client_1.service.procedure;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Процедура для вычисления суммы двух чисел
 */
@Slf4j
@Component("calculateSum")
public class CalculateSumProcedure implements ProcedureExecutor {

    @Override
    public Map<String, Object> execute(Map<String, Object> parameters) {
        log.debug("Executing calculateSum with parameters: {}", parameters);

        Number a = (Number) parameters.get("a");
        Number b = (Number) parameters.get("b");

        if (a == null || b == null) {
            throw new IllegalArgumentException("Parameters 'a' and 'b' are required");
        }

        double sum = a.doubleValue() + b.doubleValue();

        Map<String, Object> result = new HashMap<>();
        result.put("sum", sum);
        result.put("operation", "addition");

        log.debug("CalculateSum result: {}", result);
        return result;
    }
}
