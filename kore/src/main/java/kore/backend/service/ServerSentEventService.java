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
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Service
public class ServerSentEventService {

    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    // ── Conexão ───────────────────────────────────────────────────────────────

    public SseEmitter connect() {
        SseEmitter emitter = new SseEmitter(180_000L);
        emitters.add(emitter);

        emitter.onCompletion(() -> {
            log.info("Emitter completado. Removendo da lista.");
            emitters.remove(emitter);
        });

        emitter.onTimeout(() -> {
            log.info("Emitter expirado. Removendo.");
            emitter.complete();
            emitters.remove(emitter);
        });

        emitter.onError(ex -> {
            log.warn("Erro no emitter: {}. Removendo.", ex.getMessage());
            emitters.remove(emitter);
        });

        // Envia um evento inicial para confirmar a conexão ao cliente
        try {
            emitter.send(SseEmitter.event().comment("connected"));
        } catch (IOException e) {
            log.warn("Falha ao enviar confirmação de conexão: {}", e.getMessage());
            emitters.remove(emitter);
        }

        log.info("Cliente conectado. Total de clientes ativos: {}", emitters.size());
        return emitter;
    }

    // ── Envio de notificação ──────────────────────────────────────────────────

    public void sendNotification(Notificacao notificacao, Agendamento agendamento) {
        if (emitters.isEmpty()) {
            log.info("Nenhum cliente SSE conectado. Notificação não enviada.");
            return;
        }

        Map<String, Object> message = new HashMap<>();
        message.put("notificacao", notificacao);
        message.put("agendamento", new AgendamentoResponseDTO(agendamento));

        List<SseEmitter> deadEmitters = new ArrayList<>();

        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().data(message));

            } catch (IllegalStateException e) {
                // Spring encapsula "Broken pipe" / "Túnel quebrado" aqui
                // O emitter já está morto — remove silenciosamente
                log.warn("Emitter inativo (IllegalStateException): {}. Removendo.", e.getMessage());
                deadEmitters.add(emitter);

            } catch (IOException e) {
                // Falha de I/O direta (conexão fechada, reset, etc.)
                log.warn("Falha de I/O no emitter: {}. Removendo.", e.getMessage());
                deadEmitters.add(emitter);

            } catch (Exception e) {
                // Qualquer outra falha inesperada — loga com stack trace completo
                log.error("Erro inesperado ao enviar SSE para emitter. Removendo.", e);
                deadEmitters.add(emitter);
            }
        }

        if (!deadEmitters.isEmpty()) {
            emitters.removeAll(deadEmitters);
            log.info("{} emitter(s) removido(s). Total ativo: {}", deadEmitters.size(), emitters.size());
        }
    }

    // ── Heartbeat (opcional mas recomendado) ──────────────────────────────────

    /**
     * Chame este método via @Scheduled a cada 25-30s para manter conexões vivas
     * e detectar clientes desconectados antes de uma notificação real falhar.
     * <p>
     * Exemplo no seu scheduler:
     *
     * @Scheduled(fixedDelay = 25_000)
     * public void heartbeat() {
     * sseService.sendHeartbeat();
     * }
     */
    public void sendHeartbeat() {
        if (emitters.isEmpty()) return;

        List<SseEmitter> deadEmitters = new ArrayList<>();

        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().comment("heartbeat"));
            } catch (IllegalStateException | IOException e) {
                log.debug("Heartbeat falhou (cliente desconectado). Removendo emitter.");
                deadEmitters.add(emitter);
            } catch (Exception e) {
                log.warn("Erro inesperado no heartbeat. Removendo emitter.", e);
                deadEmitters.add(emitter);
            }
        }

        if (!deadEmitters.isEmpty()) {
            emitters.removeAll(deadEmitters);
        }
    }
}