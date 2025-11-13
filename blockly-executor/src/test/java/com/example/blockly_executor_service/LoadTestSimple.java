package com.example.blockly_executor_service;

import com.example.blockly_executor_service.dto.ExecutionRequestDto;
import com.example.blockly_executor_service.model.ExecutionResult;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Slf4j
public class LoadTestSimple {

    @LocalServerPort
    private int port;

    @Autowired
    private RestTemplate restTemplate;

    @Test
    public void runLoadTest() throws InterruptedException {
        log.info("=== ПРОСТОЙ ТЕСТ АСИНХРОННОСТИ ===");
        log.info("Цель: измерить время выполнения скриптов vs общее время программы");
        log.info("Конфигурация: 5 потоков, 10 запросов на поток = 50 запросов");
        log.info("Используемый порт: {}", port);

        final int NUM_THREADS = 5;
        final int REQUESTS_PER_THREAD = 10;
        final int TOTAL_REQUESTS = NUM_THREADS * REQUESTS_PER_THREAD;

        // Статистика
        final AtomicInteger successfulRequests = new AtomicInteger(0);
        final AtomicInteger failedRequests = new AtomicInteger(0);
        final List<Long> scriptExecutionTimes = Collections.synchronizedList(new ArrayList<>());

        ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);
        CountDownLatch latch = new CountDownLatch(NUM_THREADS);

        // 1. НАЧАЛО ИЗМЕРЕНИЯ ОБЩЕГО ВРЕМЕНИ
        long programStartTime = System.currentTimeMillis();
        log.info("ПРОГРАММА НАЧАЛАСЬ в {}", new Date(programStartTime));

        // Простейший скрипт
        String simpleScript = "1 + 2";

        // Запускаем потоки
        for (int i = 0; i < NUM_THREADS; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    for (int j = 0; j < REQUESTS_PER_THREAD; j++) {
                        try {
                            ExecutionRequestDto request = ExecutionRequestDto.builder()
                                    .script(simpleScript)
                                    .parameters(Map.of())
                                    .headers(Map.of("threadId", threadId, "requestNumber", j))
                                    .requestId("async-test-" + threadId + "-" + j)
                                    .build();

                            String url = "http://localhost:" + port + "/api/v1/scripts/execute";

                            ResponseEntity<ExecutionResult> response =
                                    restTemplate.postForEntity(url, request, ExecutionResult.class);

                            if (response.getStatusCode().is2xxSuccessful() &&
                                    response.getBody() != null &&
                                    response.getBody().getStatus() == ExecutionResult.ExecutionStatus.SUCCESS) {

                                successfulRequests.incrementAndGet();

                                // 2. ВРЕМЯ ВЫПОЛНЕНИЯ СКРИПТА (из ответа сервера)
                                Long scriptExecutionTime = response.getBody().getExecutionTime();
                                if (scriptExecutionTime != null) {
                                    scriptExecutionTimes.add(scriptExecutionTime);
                                }


                            } else {
                                failedRequests.incrementAndGet();
                            }

                        } catch (Exception e) {
                            failedRequests.incrementAndGet();
                            log.error("Thread {} - Request {} failed: {}", threadId, j, e.getMessage());
                        }
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        // Ждем завершения всех потоков
        latch.await();
        long programEndTime = System.currentTimeMillis();
        long totalProgramTime = programEndTime - programStartTime;

        executor.shutdown();

        // 3. КОНЕЦ ИЗМЕРЕНИЯ ОБЩЕГО ВРЕМЕНИ
        log.info("ПРОГРАММА ЗАКОНЧИЛАСЬ в {}", new Date(programEndTime));
        log.info("ОБЩЕЕ ВРЕМЯ ПРОГРАММЫ: {}ms ({}s)",
                totalProgramTime, String.format("%.2f", totalProgramTime / 1000.0));

        // Выводим результаты
        printResults(totalProgramTime, TOTAL_REQUESTS, successfulRequests, failedRequests, scriptExecutionTimes);
    }

    private void printResults(long totalProgramTime, int totalRequests,
                              AtomicInteger successfulRequests, AtomicInteger failedRequests,
                              List<Long> scriptExecutionTimes) {

        log.info("\n=== РЕЗУЛЬТАТЫ ТЕСТА ===");
        log.info("Общее время программы: {}ms", totalProgramTime);
        log.info("Всего запросов: {}", totalRequests);
        log.info("Успешных: {}", successfulRequests.get());
        log.info("Неудачных: {}", failedRequests.get());

        if (!scriptExecutionTimes.isEmpty()) {
            // Анализ времени выполнения скриптов
            double avgScriptTime = scriptExecutionTimes.stream().mapToLong(Long::longValue).average().orElse(0.0);
            long minScriptTime = scriptExecutionTimes.stream().mapToLong(Long::longValue).min().orElse(0);
            long maxScriptTime = scriptExecutionTimes.stream().mapToLong(Long::longValue).max().orElse(0);

            log.info("\n=== ВРЕМЯ ВЫПОЛНЕНИЯ СКРИПТОВ ===");
            log.info("Среднее время скриптов: {}ms", String.format("%.2f", avgScriptTime));
            log.info("Минимум: {}ms", minScriptTime);
            log.info("Максимум: {}ms", maxScriptTime);



        }

        log.info("\n=== ТЕСТ ЗАВЕРШЕН ===");
    }
}