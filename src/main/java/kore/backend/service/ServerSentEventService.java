package kore.backend.service;

import jakarta.annotation.PostConstruct;
import kore.backend.dto.AgendamentoResponseDTO;
import kore.backend.model.Agendamento;
import kore.backend.model.Notificacao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class ServerSentEventService {

    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    @PostConstruct
    public void startHeartbeat() {
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(
                () -> sendNotification(Notificacao.builder().build(), Agendamento.builder().build()),
                30, 30, TimeUnit.SECONDS
        );
    }

    public SseEmitter connect() {
        SseEmitter emitter = new SseEmitter(180_000L);

        this.emitters.add(emitter);

        emitter.onCompletion(() -> {
            log.info("Emitter completado. Removendo da lista.");
            this.emitters.remove(emitter);
        });

        emitter.onTimeout(() -> {
            log.info("Emitter expirado. Sinalizando cliente e removendo.");
            emitter.complete();
            this.emitters.remove(emitter);
        });

        emitter.onError((ex) -> {
            log.warn("Erro no emitter: {}. Removendo.", ex.getMessage());
            this.emitters.remove(emitter);
        });

        log.info("Cliente conectado. Total de clientes: {}", emitters.size());
        return emitter;
    }

    public void sendNotification(Notificacao notificacao, Agendamento agendamento) {
        List<SseEmitter> deadEmitters = new ArrayList<>();

        HashMap<Object, Object> message = new HashMap<>();
        message.put("notificacao", notificacao);
        message.put("agendamento", new AgendamentoResponseDTO(agendamento));

        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().data(message));
            } catch (IOException e) {
                log.warn("Falha ao enviar para emitter. Marcando para remoção.");
                deadEmitters.add(emitter);
            }
        }

        emitters.removeAll(deadEmitters);
    }
}