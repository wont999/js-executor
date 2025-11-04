package com.example.common.model;

import lombok.Builder;

import java.util.Map;

@Builder
public record ProcedureRequestDto<T>(
        String clientType,
        T parameters,
        String procedureName
) {}