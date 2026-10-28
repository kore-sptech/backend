package kore.backend.events;

import kore.backend.model.enums.StatusAgendamento;
import kore.backend.messaging.NotificationMessage;
import kore.backend.messaging.NotificationOutboxService;
import kore.backend.repository.AgendamentoRepository;
import kore.backend.service.ServerSentEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class AgendamentoScheduler {

    private final ServerSentEventService sseService;

    private final AgendamentoRepository agendamentoRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final NotificationOutboxService notificationOutboxService;

    @Value("${app.rabbitmq.routing-key.appointment}")
    private String appointmentRoutingKey;

    @Scheduled(fixedDelayString = "${app.notification.appointment-poll-interval-ms:10000}")
    public void verificarAgendamentosProximos() {
        LocalDateTime agora = LocalDateTime.now();
        LocalDateTime janelaFim = agora.plusMinutes(15);

        agendamentoRepository
                .findByInicioBetweenAndStatus(agora, janelaFim, StatusAgendamento.PENDENTE)
                .forEach(agendamento -> {
                    // Mantém evento in-process para SSE direto no kore
                    eventPublisher.publishEvent(new AgendamentoProximoEvent(this, agendamento.getId()));

                    // Enfileira na outbox para persistência via RabbitMQ no microsserviço de notificações
                    NotificationMessage msg = new NotificationMessage(
                            "appointment-upcoming:" + agendamento.getId(),
                            1,
                            "APPOINTMENT_UPCOMING",
                            "Próxima Sessão: " + agendamento.getCliente(),
                            String.format("Sessão agendada para as %tT.", agendamento.getInicio()),
                            "APPOINTMENT",
                            "INFO",
                            agendamento.getId(),
                            agendamento.getCliente(),
                            agendamento.getInicio(),
                            null,
                            agendamento.getUsuario() != null ? agendamento.getUsuario().getId() : null,
                            null,
                            null,
                            null,
                            "INFORMATIVO",
                            "APPOINTMENT",
                            agendamento.getId(),
                            "VER_AGENDAMENTO"
                    );
                    notificationOutboxService.enqueue(msg, appointmentRoutingKey);

                    log.info("[NOTIF-EMIT-KORE] Notificação de agendamento próximo emitida | EventId: {} | Agendamento ID: {} | Cliente: {} | Horário: {}",
                            msg.eventId(), agendamento.getId(), agendamento.getCliente(), agendamento.getInicio());
                });
    }

    @Scheduled(fixedDelay = 25_000)
    public void heartbeat() {
        sseService.sendHeartbeat();
    }
}
