package com.example.blockly_executor_service.controller;

import com.example.blockly_executor_service.dto.ExecutionRequestDto;
import com.example.blockly_executor_service.dto.ExecutionResponseDto;
import com.example.blockly_executor_service.model.ExecutionRequest;
import com.example.blockly_executor_service.model.ExecutionResult;
import com.example.blockly_executor_service.service.JavaScriptExecutorService;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/scripts")
@RequiredArgsConstructor
public class ScriptExecutionController {

    private final JavaScriptExecutorService javaScriptExecutorService;

    @PostMapping("/execute")
    public ResponseEntity<ExecutionResponseDto> executeScript(@RequestBody ExecutionRequestDto requestDto){
        //log.info("Received script execution request: {}", requestDto.getRequestId());

        ExecutionRequest request = ExecutionRequest.builder()
                .script(requestDto.getScript())
                .params(requestDto.getParameters())
                .headers(requestDto.getHeaders())
                .requestId(requestDto.getRequestId()!=null ? requestDto.getRequestId(): UUID.randomUUID().toString())
                .build();

        ExecutionResult result = javaScriptExecutorService.executeScript(request);

        ExecutionResponseDto responseDto = ExecutionResponseDto.builder()
                .requestId(result.getRequestId())
                .result(result.getResult())
                .errorMessage(result.getErrorMessage())
                .status(result.getStatus())
                .executionTime(result.getExecutionTime())
                .startTime(result.getStartTime())
                .endTime(result.getEndTime())
                .logs(result.getLogs())
                .build();

        //log.info("Script execution completed: {} - {} - {} - {}", result.getResult(), result.getErrorMessage(), result.getStatus(), result.getExecutionTime());

        return ResponseEntity.ok(responseDto);
    }

    @PostMapping("/validate")
    public ResponseEntity<ValidationResponse> validateScript(@RequestBody ValidationRequest request){
        log.info("Received script validation: {}", request.getScript());

        boolean isValid = javaScriptExecutorService.validateScript(request.getScript());

        ValidationResponse response = ValidationResponse.builder()
                .script(request.getScript())
                .isValid(isValid)
                .message(isValid ? "Script is valid" : "Script is invalid")
                .build();

        return ResponseEntity.ok(response);
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ValidationRequest {
        private String script;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ValidationResponse {
        private String script;
        private boolean isValid;
        private String message;
    }
}
