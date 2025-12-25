package com.example.blockly_executor_service.service;

import com.example.blockly_executor_service.dao.DatabaseAccessor;
import com.example.blockly_executor_service.model.ExecutionRequest;
import com.example.blockly_executor_service.model.ExecutionResult;
import com.example.blockly_executor_service.repository.ScriptExecutionLogRepository;
import com.example.common.exception.ProcedureExecutionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

import javax.script.*;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.*;

@Slf4j
@Service
@EnableAsync(proxyTargetClass = true)
public class JavaScriptExecutorService implements ScriptExecutionService {

    private static final long SCRIPT_TIMEOUT_SECONDS = 10;
    private final ExecutorService scriptExecutor = Executors.newCachedThreadPool();
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

        var scriptEngine = scriptEngineManager.getEngineByName("graal.js");
        if (scriptEngine == null) {
            throw new ProcedureExecutionException("GraalVM JavaScript engine not found");
        }

        Instant startTime = Instant.now();
        String requestId = request.getRequestId() != null ? request.getRequestId() : UUID.randomUUID().toString();
        String tenantId = (String) request.getHeaders().get("tenantId");

        if (tenantId == null || tenantId.isEmpty()) {
            throw new SecurityException("Tenant ID is required but not provided");
        }

        log.info("Executing script for tenant: {}", tenantId);

        try{
            //Разрешает JavaScript коду вызывать Java методы
            scriptEngine.getContext().setAttribute("polyglot.js.allowHostAccess", true, ScriptContext.ENGINE_SCOPE);
            //Запрещает JavaScript коду создавать новые Java объекты
            scriptEngine.getContext().setAttribute("polyglot.js.allowHostClassLookup", false, ScriptContext.ENGINE_SCOPE);

            // Создаем DatabaseAccessor для доступа к БД с изоляцией по tenant
            DatabaseAccessor dbAccessor = new DatabaseAccessor(tenantId, jdbcTemplate);
            scriptEngine.put("DB", dbAccessor);

            if(request.getParams() != null){
                request.getParams().forEach(scriptEngine::put);
            }

            Future<Object> future = scriptExecutor.submit(() -> scriptEngine.eval(request.getScript()));
            Object result;
            try {
                result = future.get(SCRIPT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            } catch (TimeoutException e) {
                future.cancel(true);
                throw new RuntimeException("Script execution timeout after " + SCRIPT_TIMEOUT_SECONDS + " seconds");
            }

            Instant endTime = Instant.now();
            Long executionTime = Duration.between(startTime, endTime).toMillis();


            loggingService.saveLogAsync(request, startTime, endTime, executionTime,
                   ExecutionResult.ExecutionStatus.SUCCESS, null);

            return ExecutionResult.builder()
                    .requestId(requestId)
                    .result(result)
                    .status(ExecutionResult.ExecutionStatus.SUCCESS)
                    .executionTime(executionTime)
                    .startTime(startTime)
                    .endTime(endTime)
                    .build();

        } catch (ExecutionException e) {
            Instant endTime = Instant.now();
            Long executionTime = Duration.between(startTime, endTime).toMillis();
            String errorMsg = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();

            log.error("SCRIPT_ERROR - RequestId: {} - {} - Script failed in {}ms at {}",
                requestId, errorMsg, executionTime, endTime);
            loggingService.saveLogAsync(request, startTime, endTime, executionTime, ExecutionResult.ExecutionStatus.ERROR, errorMsg);

            return ExecutionResult.builder()
                    .requestId(requestId)
                    .errorMessage(errorMsg)
                    .status(ExecutionResult.ExecutionStatus.ERROR)
                    .executionTime(executionTime)
                    .startTime(startTime)
                    .endTime(endTime)
                    .build();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            Instant endTime = Instant.now();
            Long executionTime = Duration.between(startTime, endTime).toMillis();

            log.error("SCRIPT_INTERRUPTED - RequestId: {} - Script interrupted in {}ms", requestId, executionTime);
            loggingService.saveLogAsync(request, startTime, endTime, executionTime, ExecutionResult.ExecutionStatus.ERROR, "Script execution interrupted");

            return ExecutionResult.builder()
                    .requestId(requestId)
                    .errorMessage("Script execution interrupted")
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
            throw new ProcedureExecutionException("GraalVM JavaScript engine not found");
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
