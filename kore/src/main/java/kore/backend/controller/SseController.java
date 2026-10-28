package kore.backend.controller;

import kore.backend.service.ServerSentEventService;
import lombok.extern.slf4j.Slf4j;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@RestController
@RequestMapping("/sse")
@Tag(name = "SSE (Server-Sent Events)", description = "Streaming de eventos em tempo real para clientes")
public class SseController {

    private final ServerSentEventService serverSentEventService;

    public SseController(ServerSentEventService serverSentEventService) {
        this.serverSentEventService = serverSentEventService;
    }

    @Operation(summary = "Stream de eventos SSE", description = "Estabelece conexão Server-Sent Events para notificações em tempo real")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Conexão estabelecida com sucesso")
    })
    @CrossOrigin(origins = "${front-end.url}")
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamUpdates() {
        log.info("Novo cliente conectado para SSE");
        return serverSentEventService.connect();
    }
}