package kore.notification.consumer;

import kore.notification.application.repositories.INotificationRepository;
import kore.notification.application.useCases.EmitNotificationUseCase;
import kore.notification.application.useCases.EmitNotificationUseCase.NotificationRequest;
import kore.notification.messaging.NotificationMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationConsumer {

    private final EmitNotificationUseCase emitNotificationUseCase;
    private final INotificationRepository notificationRepository;

    @RabbitListener(queues = "${app.rabbitmq.queue}")
    public void receiveMessage(NotificationMessage event) {
        if (event == null
                || !StringUtils.hasText(event.eventId())
                || event.eventVersion() != 1
                || !StringUtils.hasText(event.eventType())
                || !StringUtils.hasText(event.title())
                || !StringUtils.hasText(event.message())
                || !StringUtils.hasText(event.type())
                || !StringUtils.hasText(event.level())) {
            throw new IllegalArgumentException("Evento de notificação inválido");
        }
        if ("APPOINTMENT_UPCOMING".equals(event.eventType()) || "APPOINTMENT_CANCELLED".equals(event.eventType())) {
            if (event.appointmentId() == null || !"APPOINTMENT".equals(event.type())) {
                throw new IllegalArgumentException("Evento de agendamento inválido");
            }
        } else if ("STOCK_BELOW_THRESHOLD".equals(event.eventType())) {
            if (event.productId() == null
                    || event.availableQuantity() == null
                    || event.threshold() == null
                    || !"STOCK".equals(event.type())) {
                throw new IllegalArgumentException("Evento de estoque inválido");
            }
        } else if ("ITEM_EXPIRING_SOON".equals(event.eventType()) || "ITEM_EXPIRED_ACTIVE".equals(event.eventType())) {
            if (event.productId() == null || !"STOCK".equals(event.type())) {
                throw new IllegalArgumentException("Evento de validade de estoque inválido");
            }
        } else {
            throw new IllegalArgumentException("Tipo de evento desconhecido: " + event.eventType());
        }

        if (notificationRepository.existsBySourceEventId(event.eventId())) {
            log.info("[NOTIF-CONSUME] Evento {} já processado; ignorando reentrega.", event.eventId());
            return;
        }

        log.info("[NOTIF-CONSUME] Evento de notificação recebido e consumido | EventId: {} | EventType: {} | Título: {} | Mensagem: {}",
                event.eventId(), event.eventType(), event.title(), event.message());

        emitNotificationUseCase.execute(new NotificationRequest(
                event.title(),
                event.message(),
                event.type(),
                event.level(),
                event.eventId()));
        log.info("[NOTIF-CONSUME] Notificação persistida com sucesso para evento {}", event.eventId());
    }

}
