package com.example.common.model;

import lombok.Builder;

import java.util.Map;

import lombok.Builder;

@Builder
public record ProcedureResponse<T>(
        String requestId,
        boolean success,
        T result,
        String errorMessage
) {}