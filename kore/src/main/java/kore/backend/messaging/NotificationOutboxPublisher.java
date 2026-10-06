package kore.backend.messaging;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationOutboxPublisher {

    private final NotificationOutboxRepository repository;
    private final RabbitTemplate rabbitTemplate;

    @Value("${app.rabbitmq.exchange}")
    private String exchange;

    @Value("${app.rabbitmq.confirm-timeout-ms:5000}")
    private long confirmTimeoutMs;

    @Scheduled(fixedDelayString = "${app.rabbitmq.outbox-poll-interval-ms:1000}")
    public void publishPendingMessages() {
        repository.findByPublishedAtIsNullOrderByCreatedAtAsc(PageRequest.of(0, 100))
                .forEach(this::publish);
    }

    private void publish(NotificationOutboxMessage message) {
        try {
            CorrelationData correlationData = new CorrelationData(message.getEventId());
            rabbitTemplate.send(exchange, message.getRoutingKey(),
                    MessageBuilder.withBody(message.getPayload().getBytes(java.nio.charset.StandardCharsets.UTF_8))
                            .setContentType(MessageProperties.CONTENT_TYPE_JSON)
                            .setMessageId(message.getEventId())
                            .build(),
                    correlationData);

            CorrelationData.Confirm confirm = correlationData.getFuture()
                    .get(confirmTimeoutMs, TimeUnit.MILLISECONDS);
            if (!confirm.isAck()) {
                throw new IllegalStateException("RabbitMQ rejeitou a mensagem: " + confirm.getReason());
            }
            if (correlationData.getReturned() != null) {
                throw new IllegalStateException("RabbitMQ não roteou a mensagem: "
                        + correlationData.getReturned().getReplyText());
            }

            markPublished(message.getId());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            log.error("Publicação do evento {} interrompida.", message.getEventId(), exception);
            throw new IllegalStateException("Publicação de evento de notificação interrompida", exception);
        } catch (Exception exception) {
            log.error("Falha ao publicar evento {}. Será tentado novamente.", message.getEventId(), exception);
        }
    }

    private void markPublished(Long id) {
        repository.findById(id).ifPresent(message -> {
            message.setPublishedAt(LocalDateTime.now());
            repository.save(message);
        });
    }
}
