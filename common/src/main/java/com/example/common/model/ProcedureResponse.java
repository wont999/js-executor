package com.example.common.model;

import lombok.Builder;

import java.util.UUID;

@Builder
public record ProcedureResponse<T>(
        UUID requestId,
        boolean success,
        T result,
        String errorMessage
) {
}