package com.example.blockly_executor_service.model;

public record BlocklyScriptParams(
        String scriptId,
        String script,
        Object input
) {}
