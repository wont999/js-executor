package com.example.common.model;

import lombok.Builder;

@Builder
public record ProcedureRequest<T>(
        String requestId,
        String clientType,
        String procedureName,
        T parameters,
        String replyTo
) {}