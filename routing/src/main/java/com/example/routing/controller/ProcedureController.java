package com.example.routing.controller;

import com.example.common.model.ProcedureRequestDto;
import com.example.common.model.ProcedureResponse;
import com.example.routing.service.ProcedureGatewayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.errors.TimeoutException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST API для вызова процедур через gateway.
 * Принимает HTTP-запросы и синхронно возвращает результаты.
 */
@RestController
@RequestMapping("/api/procedures")
@Slf4j
@RequiredArgsConstructor
public class ProcedureController {

    final ProcedureGatewayService gatewayService;

    @PostMapping("/execute")
    public ResponseEntity<ProcedureResponse> executeProcedure(@RequestBody ProcedureRequestDto request) {
        log.info("Received request for procedure: {} (client: {})",
                request.procedureName(), request.clientType());

        ProcedureResponse response = gatewayService.executeProcedure(request);

        if (!response.success()) {
            throw new TimeoutException();
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
