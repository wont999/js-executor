package com.example.routing.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Слушатель события готовности Spring контекста.
 * Ожидает загрузки реестра сервисов из Eureka при старте приложения.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EurekaReadyListener implements ApplicationListener<ContextRefreshedEvent> {

    private final DiscoveryClient discoveryClient;
    private static final int MAX_WAIT_SECONDS = 30;
    private static final int CHECK_INTERVAL_SECONDS = 2;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        log.info("Application context refreshed. Waiting for Eureka registry to be populated...");
        
        int attempts = 0;
        int maxAttempts = MAX_WAIT_SECONDS / CHECK_INTERVAL_SECONDS;
        
        while (attempts < maxAttempts) {
            try {
                List<String> services = discoveryClient.getServices();
                
                if (!services.isEmpty()) {
                    log.info("✓ Eureka registry is ready! Found {} service(s): {}", 
                        services.size(), services);
                    return;
                }
                
                attempts++;
                log.debug("Waiting for Eureka registry... Attempt {}/{}", attempts, maxAttempts);
                TimeUnit.SECONDS.sleep(CHECK_INTERVAL_SECONDS);
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Interrupted while waiting for Eureka registry");
                return;
            } catch (Exception e) {
                log.warn("Error checking Eureka registry: {}", e.getMessage());
            }
        }
        
        log.warn("⚠ Eureka registry is still empty after {} seconds. " +
                "Service discovery may not work until services are registered.", MAX_WAIT_SECONDS);
    }
}
