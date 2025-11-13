package com.example.blockly_executor_service.service;

import com.example.blockly_executor_service.model.ExecutionRequest;
import com.example.blockly_executor_service.model.ExecutionResult;

public interface ScriptExecutionService {
    ExecutionResult executeScript(ExecutionRequest request);
    boolean validateScript(String script);

}
