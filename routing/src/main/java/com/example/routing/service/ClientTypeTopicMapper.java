package com.example.routing.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import com.example.routing.exception.ServiceNotFoundException;

import java.util.List;

/**
 * Динамическая маршрутизация клиентов на топики Kafka.
 * Автоматически проверяет наличие сервиса через Discovery Service
 * и генерирует имя топика на основе соглашения об именовании.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ClientTypeTopicMapper {

    private final ServiceDiscoveryService serviceDiscoveryService;

    /**
     * Получает имя топика для заданного типа клиента
     * Автоматически проверяет наличие сервиса в Eureka
     * @param clientType тип клиента (например, CLIENT_1)
     * @return имя топика (например, client-1-procedures)
     * @throws ServiceNotFoundException если сервис не найден в Eureka
     */
    public String getTopicForClientType(String clientType) {
        String serviceName = getServiceNameForClientType(clientType);
        
        // Проверяем наличие сервиса через Discovery Service
        log.info("Checking service availability for client type '{}' (service: '{}')", clientType, serviceName);
        boolean isAvailable = serviceDiscoveryService.isServiceAvailable(serviceName);
        
        if (!isAvailable) {
            List<String> availableServices = serviceDiscoveryService.getAllRegisteredServices();
            log.error("Service '{}' for client type '{}' is not available in Eureka", serviceName, clientType);
            log.error("Available services in Eureka: {}", availableServices);
            
            throw new ServiceNotFoundException(serviceName, 
                String.format("Service '%s' for client type '%s' is not registered or available. Available services: %s", 
                    serviceName, clientType, availableServices));
        }
        
        log.info("Service '{}' is available, proceeding with request", serviceName);
        
        return generateTopicName(clientType);
    }

    /**
     * Проверяет, существует ли сервис для данного типа клиента в Eureka
     * @param clientType тип клиента
     * @return true если сервис зарегистрирован и доступен
     */
    public boolean isClientTypeSupported(String clientType) {
        String serviceName = getServiceNameForClientType(clientType);
        return serviceDiscoveryService.isServiceAvailable(serviceName);
    }

    /**
     * Получает имя сервиса для заданного типа клиента
     * Преобразует CLIENT_1 -> CLIENT-1 для поиска в Eureka
     * @param clientType тип клиента (например, CLIENT_1)
     * @return имя сервиса для регистрации в Eureka (например, CLIENT-1)
     */
    public String getServiceNameForClientType(String clientType) {
        // Заменяем подчеркивание на дефис и переводим в верхний регистр
        // CLIENT_1 -> CLIENT-1
        return clientType.toUpperCase().replace("_", "-");
    }
    
    /**
     * Генерирует имя топика на основе типа клиента
     * @param clientType тип клиента (например, CLIENT_1)
     * @return имя топика (например, client-1-procedures)
     */
    private String generateTopicName(String clientType) {
        // CLIENT_1 -> client-1-procedures
        String topic = clientType.toLowerCase().replace("_", "-") + "-procedures";
        log.debug("Generated topic name '{}' for client type '{}'", topic, clientType);
        return topic;
    }
}
