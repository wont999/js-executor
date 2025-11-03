package com.example.routing.exception;

/**
 * Exception thrown when a requested service is not found in Eureka Discovery
 */
public class ServiceNotFoundException extends RuntimeException {
    
    private final String serviceName;

    public ServiceNotFoundException(String serviceName) {
        super(String.format("Service '%s' is not registered or available in Eureka Discovery", serviceName));
        this.serviceName = serviceName;
    }

    public ServiceNotFoundException(String serviceName, String message) {
        super(message);
        this.serviceName = serviceName;
    }

    public String getServiceName() {
        return serviceName;
    }
}
