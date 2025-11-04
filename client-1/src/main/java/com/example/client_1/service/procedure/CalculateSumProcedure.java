package com.example.client_1.service.procedure;

import com.example.client_1.model.SumParams;
import com.example.client_1.model.SumResult;
import com.example.common.ProcedureExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


@Slf4j
@Component("calculateSum")
public class CalculateSumProcedure implements ProcedureExecutor<SumParams, SumResult> {

    @Override
    public SumResult execute(SumParams parameters) {
        log.info("Executing calculateSum with parameters: {}", parameters);

        Number a = parameters.a();
        Number b = parameters.b();

        if (a == null || b == null) {
            log.error("Missing required parameters. Received: a={}, b={}", a, b);
            throw new IllegalArgumentException("Parameters 'a' and 'b' are required");
        }

        double sum = a.doubleValue() + b.doubleValue();
        SumResult result = new SumResult(sum, "addition");

        log.info("CalculateSum result: {}", result);
        return result;
    }
}