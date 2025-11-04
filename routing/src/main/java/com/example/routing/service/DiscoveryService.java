package com.example.routing.service;

import com.example.common.exception.ServiceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.example.common.exception.ExceptionMessages.SERVICE_NOT_FOUND;

/**
 * Service for working with Eureka service discovery.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DiscoveryService {

    final DiscoveryClient discoveryClient;


    public List<ServiceInstance> getServiceInstances(String serviceName) {
        var name = serviceName.toLowerCase();
        var instances = discoveryClient.getInstances(name);
        if (instances != null && !instances.isEmpty()) {
            log.info("Found {} instance(s) for service '{}'", instances.size(), name);
            return instances;
        }
        log.warn("No instances found for service '{}'", name);
        throw new ServiceNotFoundException(SERVICE_NOT_FOUND.formatted(name));
    }
}