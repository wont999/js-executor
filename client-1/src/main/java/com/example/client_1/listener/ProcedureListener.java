package com.example.client_1.listener;

import com.example.client_1.service.ProcedureWorkerService;
import com.example.common.model.ProcedurePayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProcedureListener {

    final ProcedureWorkerService procedureWorkerService;

    @KafkaListener(topics = "client-1-procedures", groupId = "worker-client-1")
    public void handleClient1Procedure(ProcedurePayload<?> request) {
        log.info("Processing CLIENT_1 with request: {}", request);

        var response = procedureWorkerService.executeProcedure(request);
        procedureWorkerService.sendResponse(request.replyTo(), request.requestId(), response);
    }

}
