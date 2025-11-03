package com.example.common.model;

import lombok.Builder;

import java.util.Map;

@Builder
public record ProcedureResponse(
        String requestId,
        boolean success,
        Map<String, Object> result,
        String errorMessage
) {}