package com.example.common.model;

import lombok.Builder;

import java.util.Map;

@Builder
public record ProcedureRequestDto(
        String clientType,
        Map<String, Object> parameters,
        String procedureName
) {}