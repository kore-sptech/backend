package kore.backend.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Service
public class ServerSentEventService {
    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public SseEmitter connect() {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE); // Mantém a conexão aberta
        this.emitters.add(emitter);

        emitter.onCompletion(() -> this.emitters.remove(emitter));
        emitter.onTimeout(() -> this.emitters.remove(emitter));

        log.info("Cliente conectado. Total de clientes: {}", emitters.size());
        return emitter;
    }

    public void sendNotification(String message) {
        for (SseEmitter emitter : emitters) {
            try {
                log.info("Enviando notificação para um cliente: {}", message);
                emitter.send(SseEmitter.event().data(message));
            } catch (IOException e) {
                log.info("Erro ao enviar notificação para um cliente. Removendo emitter.");
                emitters.remove(emitter);
            }
        }
    }
}
