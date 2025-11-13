package com.example.blockly_executor_service.service.procedure;


import com.example.blockly_executor_service.model.ExecutionRequest;
import com.example.blockly_executor_service.model.ExecutionResult;
import com.example.blockly_executor_service.service.ScriptExecutionService;
import com.example.common.ProcedureExecutor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component("executeBlocklyScript")
@RequiredArgsConstructor
public class ExecuteBlocklyScriptProcedure implements ProcedureExecutor<Map<String, Object>, Object> {

    private final ScriptExecutionService scriptExecutionService;

    @Override
    public Object execute(Map<String, Object> parameters) {
        log.info("Executing Blockly script with parameters: {}", parameters);

        try {
            String script = (String) parameters.get("script");
            String scriptId = (String) parameters.get("scriptId");

            if (script == null) {
                throw new IllegalArgumentException("Script parameter is required");
            }

            // Логируем scriptId только если он передан
            if (scriptId != null) {
                log.info("Processing script with ID: {}", scriptId);
            } else {
                log.info("Processing script (no ID provided)");
            }

            // Создаем ExecutionRequest из параметров
            ExecutionRequest request = ExecutionRequest.builder()
                    .script(script)
                    .params(parameters)
                    .headers(scriptId != null ? Map.of("scriptId", scriptId) : Map.of())
                    .build();

            // Выполняем скрипт
            ExecutionResult result = scriptExecutionService.executeScript(request);

            if (result.getStatus() == ExecutionResult.ExecutionStatus.SUCCESS) {
                Object scriptResult = result.getResult() != null ? result.getResult() : "undefined";
                
                if (scriptId != null) {
                    log.info("Script executed successfully (ID: {}): {}", scriptId, scriptResult);
                    return Map.of(
                            "success", true,
                            "result", scriptResult,
                            "scriptId", scriptId
                    );
                } else {
                    log.info("Script executed successfully: {}", scriptResult);
                    return Map.of(
                            "success", true,
                            "result", scriptResult
                    );
                }
            } else {
                String errorMessage = result.getErrorMessage() != null ? result.getErrorMessage() : "Unknown error";
                log.error("Script execution failed: {}", errorMessage);
                
                if (scriptId != null) {
                    return Map.of(
                            "success", false,
                            "error", errorMessage,
                            "scriptId", scriptId
                    );
                } else {
                    return Map.of(
                            "success", false,
                            "error", errorMessage
                    );
                }
            }

        } catch (Exception e) {
            log.error("Error executing Blockly script: {}", e.getMessage(), e);
            String scriptId = (String) parameters.get("scriptId");
            
            if (scriptId != null) {
                return Map.of(
                        "success", false,
                        "error", e.getMessage(),
                        "scriptId", scriptId
                );
            } else {
                return Map.of(
                        "success", false,
                        "error", e.getMessage()
                );
            }
        }
    }
}
