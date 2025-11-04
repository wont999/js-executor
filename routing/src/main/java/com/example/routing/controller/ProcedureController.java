package com.example.routing.controller;

import com.example.common.model.ProcedureRequestDto;
import com.example.common.model.ProcedureResponse;
import com.example.routing.service.ProcedureGatewayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;


@RestController
@RequestMapping("/api/procedures")
@Slf4j
@RequiredArgsConstructor
public class ProcedureController {

    final ProcedureGatewayService gatewayService;

    @PostMapping("/execute")
    public ResponseEntity<ProcedureResponse> executeProcedure(@RequestBody ProcedureRequestDto request) {
        log.info("Received request for procedure: {} (client: {})", request.procedureName(), request.clientType());

        var response = gatewayService.executeProcedure(request);

        log.info("Successfully executed procedure: {} (client: {})", request.procedureName(), request.clientType());

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
