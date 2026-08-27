package kore.backend.controller;

import kore.backend.service.ServerSentEventService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@RestController
@RequestMapping("/sse")
public class SseController {

    private final ServerSentEventService serverSentEventService;

    public SseController(ServerSentEventService serverSentEventService) {
        this.serverSentEventService = serverSentEventService;
    }

    @CrossOrigin(origins = "${front-end.url}")
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamUpdates() {
        log.info("Novo cliente conectado para SSE");
        return serverSentEventService.connect();
    }
}