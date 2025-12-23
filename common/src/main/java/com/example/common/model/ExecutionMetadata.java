package com.example.common.model;

import lombok.Builder;

import java.util.HashMap;
import java.util.Map;

@Builder
public record ExecutionMetadata(
        String userId,
        String tenantId,
        Map<String, String> headers
) {
    public ExecutionMetadata {
        if (headers == null) {
            headers = new HashMap<>();
        }
    }


}
