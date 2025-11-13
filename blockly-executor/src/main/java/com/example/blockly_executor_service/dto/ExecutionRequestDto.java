package com.example.blockly_executor_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecutionRequestDto {

    private String script;
    private Map<String, Object> parameters;
    private Map<String, Object> headers;
    private String requestId;
}
