package com.example.blockly_executor_service.dto;

import com.example.blockly_executor_service.model.ExecutionResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecutionResponseDto {
    private String requestId;
    private Object result;
    private String errorMessage;
    private ExecutionResult.ExecutionStatus status;
    private Long executionTime;
    private Instant startTime;
    private Instant endTime;
    private String logs;
}
