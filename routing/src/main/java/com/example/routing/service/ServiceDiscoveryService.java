package com.example.routing.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service for interacting with Eureka Discovery Client
 * to check service availability and retrieve service instances
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ServiceDiscoveryService {

    private final DiscoveryClient discoveryClient;

    /**
     * Check if a service is registered and available in Eureka
     *
     * @param serviceName the name of the service to check (case-insensitive)
     * @return true if service is registered and has at least one instance, false otherwise
     */
    public boolean isServiceAvailable(String serviceName) {
        try {
            // Логируем все доступные сервисы для диагностики
            List<String> allServices = discoveryClient.getServices();
            log.info("Checking service '{}'. Total services in registry: {}", serviceName, allServices.size());
            log.info("All registered services: {}", allServices);

            // Пробуем оба варианта: верхний и нижний регистр
            String upperCaseName = serviceName.toUpperCase();
            String lowerCaseName = serviceName.toLowerCase();

            List<ServiceInstance> instances = discoveryClient.getInstances(upperCaseName);
            if (instances == null || instances.isEmpty()) {
                // Пробуем с нижним регистром
                instances = discoveryClient.getInstances(lowerCaseName);
            }

            boolean available = instances != null && !instances.isEmpty();

            if (available) {
                log.info("Service '{}' is available with {} instance(s)", serviceName, instances.size());
                instances.forEach(instance ->
                        log.info("  - Instance: {} ({}:{})", instance.getInstanceId(), instance.getHost(), instance.getPort())
                );
            } else {
                log.warn("Service '{}' is not available in Eureka. Tried names: {}, {}",
                        serviceName, upperCaseName, lowerCaseName);
            }

            return available;
        } catch (Exception e) {
            log.error("Error checking service availability for '{}'", serviceName, e);
            return false;
        }
    }

    /**
     * Get all instances of a specific service
     *
     * @param serviceName the name of the service
     * @return list of service instances
     */
    public List<ServiceInstance> getServiceInstances(String serviceName) {
        try {
            List<ServiceInstance> instances = discoveryClient.getInstances(serviceName.toUpperCase());
            log.info("Found {} instance(s) for service '{}'",
                    instances != null ? instances.size() : 0, serviceName);
            return instances != null ? instances : List.of();
        } catch (Exception e) {
            log.error("Error retrieving instances for service '{}'", serviceName, e);
            return List.of();
        }
    }

    /**
     * Get all registered service names from Eureka
     *
     * @return list of all registered service names
     */
    public List<String> getAllRegisteredServices() {
        try {
            List<String> services = discoveryClient.getServices();
            log.debug("Retrieved {} registered service(s) from Eureka", services.size());
            return services;
        } catch (Exception e) {
            log.error("Error retrieving all services from Eureka", e);
            return List.of();
        }
    }

    /**
     * Get detailed information about a service instance
     *
     * @param serviceName the name of the service
     * @return formatted string with service details
     */
    public String getServiceInfo(String serviceName) {
        List<ServiceInstance> instances = getServiceInstances(serviceName);

        if (instances.isEmpty()) {
            return String.format("Service '%s' is not available", serviceName);
        }

        StringBuilder info = new StringBuilder();
        info.append(String.format("Service '%s' - %d instance(s):\n", serviceName, instances.size()));

        for (int i = 0; i < instances.size(); i++) {
            ServiceInstance instance = instances.get(i);
            info.append(String.format("  [%d] %s:%d (instanceId: %s)\n",
                    i + 1,
                    instance.getHost(),
                    instance.getPort(),
                    instance.getInstanceId()));
        }

        return info.toString();
    }
}
