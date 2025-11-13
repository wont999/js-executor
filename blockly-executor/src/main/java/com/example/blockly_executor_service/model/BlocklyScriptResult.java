package com.example.blockly_executor_service.model;

public record BlocklyScriptResult(
        boolean success,
        Object result,
        String errorMessage
) {}
