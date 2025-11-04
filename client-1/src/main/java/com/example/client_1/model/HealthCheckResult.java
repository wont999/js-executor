package com.example.client_1.model;

public record HealthCheckResult(
        String status,
        String service,
        long timestamp,
        long uptime
) {
}
