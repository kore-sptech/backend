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
import java.time.format.DateTimeFormatter;

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
                    notificationOutboxService.enqueue(new NotificationMessage(
                            "appointment-upcoming:" + agendamento.getId(),
                            1,
                            "APPOINTMENT_UPCOMING",
                            "Agendamento próximo",
                            String.format("Agendamento de %s começa em %s.",
                                    agendamento.getCliente(),
                                    agendamento.getInicio().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))),
                            "APPOINTMENT",
                            "INFO",
                            agendamento.getId(),
                            agendamento.getCliente(),
                            agendamento.getInicio(),
                            null,
                            agendamento.getUsuario() == null ? null : agendamento.getUsuario().getId(),
                            null,
                            null,
                            null),
                            appointmentRoutingKey);
                    eventPublisher.publishEvent(new AgendamentoProximoEvent(this, agendamento.getId()));
                    log.info("Evento de notificação enfileirado para agendamento ID: {}", agendamento.getId());
                });
    }

    @Scheduled(fixedDelay = 25_000)
    public void heartbeat() {
        sseService.sendHeartbeat();
    }
}