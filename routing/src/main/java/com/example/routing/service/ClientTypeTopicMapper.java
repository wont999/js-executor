package com.example.routing.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
@Slf4j
public class ClientTypeTopicMapper {

    final DiscoveryService discoveryService;

    final static String POSTFIX_TOPIC = "-procedures";

    public String getTopicForClientType(String clientType) {
        log.info("Checking service availability for client type '{}' (service: '{}')", clientType, clientType);

        discoveryService.getServiceInstances(clientType);

        log.info("Service '{}' is available, proceeding with request", clientType);
        return generateTopicName(clientType);
    }

    /**
     * Генерирует имя топика на основе типа клиента
     *
     * @param clientType тип клиента (например, CLIENT-1)
     * @return имя топика (например, client-1-procedures)
     */
    String generateTopicName(String clientType) {
        String topic = clientType.toLowerCase() + POSTFIX_TOPIC;
        log.debug("Generated topic name '{}' for client type '{}'", topic, clientType);
        return topic;
    }
}
