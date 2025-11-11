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

import java.util.concurrent.CompletableFuture;

import static org.springframework.http.HttpStatus.OK;

@RestController
@RequestMapping("/api/procedures")
@Slf4j
@RequiredArgsConstructor
public class ProcedureController {

    final ProcedureGatewayService gatewayService;

    @PostMapping("/execute")
    public ResponseEntity<ProcedureResponse<?>> executeProcedure(@RequestBody ProcedureRequestDto<?> request) {
        log.info("Received request for procedure: {} (client: {})", request.procedureName(), request.clientType());

        var response = gatewayService.executeProcedure(request);

        log.info("Successfully executed procedure: {} (client: {})", request.procedureName(), request.clientType());

        return ResponseEntity.status(OK).body(response);
    }

    @PostMapping("/execute-async")
    public CompletableFuture<ResponseEntity<ProcedureResponse<?>>> executeProcedureAsync(@RequestBody ProcedureRequestDto<?> request) {
        log.info("Received async request for procedure: {} (client: {})", request.procedureName(), request.clientType());

        return gatewayService.executeProcedureAsync(request)
                .<ResponseEntity<ProcedureResponse<?>>>thenApply(response -> {
                    log.info("Successfully executed async procedure: {} (client: {})", request.procedureName(), request.clientType());
                    return ResponseEntity.status(OK).body(response);
                })
                .exceptionally(ex -> {
                    log.error("Async procedure execution failed: {} (client: {})", request.procedureName(), request.clientType(), ex);
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .<ProcedureResponse<?>>body(ProcedureResponse.builder()
                                    .success(false)
                                    .errorMessage(ex.getMessage())
                                    .build());
                });
    }
}