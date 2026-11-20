package kore.backend.service;

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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Service
public class ServerSentEventService {

    private final ConcurrentHashMap<Long, List<SseEmitter>> emittersByUser = new ConcurrentHashMap<>();

    // ── Conexão por usuário ───────────────────────────────────────────────────

    public SseEmitter connect(Long usuarioId) {
        SseEmitter emitter = new SseEmitter(180_000L);
        emittersByUser.computeIfAbsent(usuarioId, k -> new CopyOnWriteArrayList<>()).add(emitter);

        emitter.onCompletion(() -> {
            log.info("Emitter completado para usuário {}. Removendo.", usuarioId);
            removeEmitter(usuarioId, emitter);
        });

        emitter.onTimeout(() -> {
            log.info("Emitter expirado para usuário {}. Removendo.", usuarioId);
            emitter.complete();
            removeEmitter(usuarioId, emitter);
        });

        emitter.onError(ex -> {
            log.warn("Erro no emitter do usuário {}: {}. Removendo.", usuarioId, ex.getMessage());
            removeEmitter(usuarioId, emitter);
        });

        try {
            emitter.send(SseEmitter.event().comment("connected"));
        } catch (IOException e) {
            log.warn("Falha ao enviar confirmação de conexão para usuário {}: {}", usuarioId, e.getMessage());
            removeEmitter(usuarioId, emitter);
        }

        log.info("Cliente conectado para usuário {}. Total de clientes para usuário: {}",
                usuarioId, emittersByUser.getOrDefault(usuarioId, List.of()).size());
        return emitter;
    }

    private void removeEmitter(Long usuarioId, SseEmitter emitter) {
        List<SseEmitter> list = emittersByUser.get(usuarioId);
        if (list != null) {
            list.remove(emitter);
            if (list.isEmpty()) {
                emittersByUser.remove(usuarioId);
            }
        }
    }

    // ── Envio de notificação para usuário específico ──────────────────────────

    public void sendNotification(Notificacao notificacao, Agendamento agendamento) {
        if (agendamento == null || agendamento.getUsuario() == null || agendamento.getUsuario().getId() == null) {
            log.warn("Agendamento sem usuário associado. SSE não enviado.");
            return;
        }

        Long usuarioId = agendamento.getUsuario().getId();
        List<SseEmitter> userEmitters = emittersByUser.get(usuarioId);

        if (userEmitters == null || userEmitters.isEmpty()) {
            log.info("Nenhum cliente SSE conectado para usuário {}. Notificação não enviada.", usuarioId);
            return;
        }

        Map<String, Object> message = new HashMap<>();
        message.put("notificacao", notificacao);
        message.put("agendamento", new AgendamentoResponseDTO(agendamento));

        List<SseEmitter> deadEmitters = new ArrayList<>();

        for (SseEmitter emitter : userEmitters) {
            try {
                emitter.send(SseEmitter.event().data(message));
            } catch (IllegalStateException e) {
                log.warn("Emitter inativo (IllegalStateException) para usuário {}: {}. Removendo.", usuarioId, e.getMessage());
                deadEmitters.add(emitter);
            } catch (IOException e) {
                log.warn("Falha de I/O no emitter do usuário {}: {}. Removendo.", usuarioId, e.getMessage());
                deadEmitters.add(emitter);
            } catch (Exception e) {
                log.error("Erro inesperado ao enviar SSE para usuário {}. Removendo.", usuarioId, e);
                deadEmitters.add(emitter);
            }
        }

        if (!deadEmitters.isEmpty()) {
            userEmitters.removeAll(deadEmitters);
            if (userEmitters.isEmpty()) {
                emittersByUser.remove(usuarioId);
            }
            log.info("{} emitter(s) removido(s) para usuário {}.", deadEmitters.size(), usuarioId);
        }
    }

    // ── Heartbeat por usuário ────────────────────────────────────────────────

    public void sendHeartbeat() {
        if (emittersByUser.isEmpty()) return;

        emittersByUser.forEach((usuarioId, userEmitters) -> {
            List<SseEmitter> deadEmitters = new ArrayList<>();
            for (SseEmitter emitter : userEmitters) {
                try {
                    emitter.send(SseEmitter.event().comment("heartbeat"));
                } catch (Exception e) {
                    deadEmitters.add(emitter);
                }
            }
            if (!deadEmitters.isEmpty()) {
                userEmitters.removeAll(deadEmitters);
                if (userEmitters.isEmpty()) {
                    emittersByUser.remove(usuarioId);
                }
            }
        });
    }
}