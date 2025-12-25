package com.example.blockly_executor_service.service;

import com.example.blockly_executor_service.model.ExecutionRequest;
import com.example.blockly_executor_service.model.ExecutionResult;
import com.example.blockly_executor_service.model.ScriptExecutionLog;
import com.example.blockly_executor_service.repository.ScriptExecutionLogRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
@Service
public class LoggingService {

    private final ScriptExecutionLogRepository scriptExecutionLogRepository;
    private final JdbcTemplate jdbcTemplate;

    public LoggingService(ScriptExecutionLogRepository scriptExecutionLogRepository, JdbcTemplate jdbcTemplate) {
        this.scriptExecutionLogRepository = scriptExecutionLogRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Async("taskExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveLogAsync(ExecutionRequest request,
                             Instant startTime,
                             Instant endTime,
                             long ms,
                             ExecutionResult.ExecutionStatus status,
                             String errorMessage) {
        log.info("DB_WRITE_START - RequestId: {}", request.getRequestId());
        try {
            // Имитируем медленную работу с БД через pg_sleep
            // jdbcTemplate.execute("SELECT pg_sleep(3)"); // 3 секунды задержка

            var executionLog = new ScriptExecutionLog();
            executionLog.setRequestId(request.getRequestId());
            executionLog.setScriptPreview(preview(request.getScript(), 500));
            executionLog.setParametersPreview(preview(request.getParams(), 1000));
            executionLog.setHeadersPreview(preview(request.getHeaders(), 1000));
            executionLog.setStatus(status);
            executionLog.setErrorMessage(preview(errorMessage, 1000));
            executionLog.setStartTime(startTime);
            executionLog.setEndTime(endTime);
            executionLog.setExecutionTime(ms);

            scriptExecutionLogRepository.save(executionLog);

            log.debug("Лог успешно сохранен для requestId: {}", request.getRequestId());

        } catch (Exception e) {
            log.error("Ошибка при сохранении лога для requestId: {}. Ошибка: {}",
                    request.getRequestId(), e.getMessage());
        }
        log.info("DB_SAVE_END - RequestId: {}", request.getRequestId());
    }

    private String preview(Object obj, int max) {
        if (obj == null) {
            return null;
        }
        var preview = String.valueOf(obj);
        return preview.length() > max ? preview.substring(0, max) : preview;
    }
}