package com.example.common.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProcedureRequest {
    String requestId;
    String clientType;
    String procedureName;
    Map<String, Object> parameters;
    String replyTo;
}
