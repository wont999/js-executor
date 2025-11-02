package com.example.common.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProcedureResponse {
    String requestId;
    boolean success;
    Map<String, Object> result;
    String errorMessage;
}
