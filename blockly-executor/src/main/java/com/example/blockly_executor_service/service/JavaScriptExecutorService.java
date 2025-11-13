package com.example.blockly_executor_service.service;

import com.example.blockly_executor_service.model.ExecutionRequest;
import com.example.blockly_executor_service.model.ExecutionResult;
import com.example.blockly_executor_service.model.ScriptExecutionLog;
import com.example.blockly_executor_service.repository.ScriptExecutionLogRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.script.*;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@EnableAsync(proxyTargetClass = true)
public class JavaScriptExecutorService implements ScriptExecutionService {

    private final ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
    private final ScriptExecutionLogRepository scriptExecutionLogRepository;
    private final JdbcTemplate jdbcTemplate;
    private final LoggingService loggingService;

    public JavaScriptExecutorService(ScriptExecutionLogRepository scriptExecutionLogRepository, JdbcTemplate jdbcTemplate, LoggingService loggingService) {
        this.scriptExecutionLogRepository = scriptExecutionLogRepository;
        this.jdbcTemplate = jdbcTemplate;
        this.loggingService = loggingService;
    }

    @Override
    public ExecutionResult executeScript(ExecutionRequest request) {

//        //TODO: убрать логи
//        log.info("=== EXECUTION REQUEST DEBUG ===");
//        log.info("Script: {}", request.getScript());
//        log.info("Params: {}", request.getParams());
//        log.info("Headers: {}", request.getHeaders());
//        log.info("Params is null: {}", request.getParams() == null);
//        log.info("Headers is null: {}", request.getHeaders() == null);




        var scriptEngine = scriptEngineManager.getEngineByName("graal.js");
        if (scriptEngine == null) {
            throw new RuntimeException("ScriptEngine not found");
        }
        log.info("ScriptEngine found: {}",scriptEngine.getClass().getName());

        Instant startTime = Instant.now();
        String requestId = request.getRequestId() != null ? request.getRequestId() : UUID.randomUUID().toString();

        try{
            log.info("SCRIPT_START - RequestId: {} - Starting script execution at {}", requestId, startTime);
            ScriptContext context = new SimpleScriptContext();

            Map<String, Object> contextData = new HashMap<>();
            if(request.getParams() != null){
                contextData.putAll(request.getParams());
            }
            if(request.getHeaders() != null){
                contextData.putAll(request.getHeaders());
            }

            contextData.forEach((key, value) -> {
                context.setAttribute(key, value, ScriptContext.ENGINE_SCOPE);
            });

            Object result = scriptEngine.eval(request.getScript(), context);

            Instant endTime = Instant.now();
            Long executionTime = Duration.between(startTime, endTime).toMillis();



            loggingService.saveLogAsync(request, startTime, endTime, executionTime,
                   ExecutionResult.ExecutionStatus.SUCCESS, null);

            log.info("SCRIPT_END - RequestId: {} - Script completed in {}ms at {}", requestId, executionTime, endTime);
            return ExecutionResult.builder()
                    .requestId(requestId)
                    .result(result)
                    .status(ExecutionResult.ExecutionStatus.SUCCESS)
                    .executionTime(executionTime)
                    .startTime(startTime)
                    .endTime(endTime)
                    .build();

        } catch (ScriptException e) {
            Instant endTime = Instant.now();
            Long executionTime = Duration.between(startTime, endTime).toMillis();

            log.error("SCRIPT_ERROR - RequestId: {} - {} - Script failed in {}ms at {}",
                requestId,e.getMessage(), executionTime, endTime);
            loggingService.saveLogAsync(request, startTime, endTime, executionTime, ExecutionResult.ExecutionStatus.ERROR, e.getMessage());

            return ExecutionResult.builder()
                    .requestId(requestId)
                    .errorMessage(e.getMessage())
                    .status(ExecutionResult.ExecutionStatus.ERROR)
                    .executionTime(executionTime)
                    .startTime(startTime)
                    .endTime(endTime)
                    .build();
        } catch (Exception e){
            Instant endTime = Instant.now();
            Long executionTime = Duration.between(startTime, endTime).toMillis();

            log.error("SCRIPT_ERROR - RequestId: {} - {} - Script failed in {}ms at {}",
                requestId,e.getMessage(), executionTime, endTime);
            loggingService.saveLogAsync(request, startTime, endTime, executionTime, ExecutionResult.ExecutionStatus.ERROR, e.getMessage());

            return ExecutionResult.builder()
                    .requestId(requestId)
                    .errorMessage("Error: " + e.getMessage())
                    .status(ExecutionResult.ExecutionStatus.ERROR)
                    .executionTime(executionTime)
                    .startTime(startTime)
                    .endTime(endTime)
                    .build();
        }
    }





    @Override
    public boolean validateScript(String script) {
        var scriptEngine = scriptEngineManager.getEngineByName("graal.js");
        if (scriptEngine == null) {
            throw new RuntimeException("ScriptEngine not found");
        }
        log.info("ScriptEngine found: {}",scriptEngine.getClass().getName());

        try{
            CompiledScript compiledScript = ((Compilable) scriptEngine).compile(script);
            return true;
        } catch (ScriptException e) {
            log.error("Script validation failed: {}", e.getMessage());
            return false;
        }
    }
}
