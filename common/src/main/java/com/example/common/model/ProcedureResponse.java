package com.example.common.model;

import lombok.Builder;

@Builder
public record ProcedureResponse<T>(
        String requestId,
        boolean success,
        T result,
        String errorMessage
) {}