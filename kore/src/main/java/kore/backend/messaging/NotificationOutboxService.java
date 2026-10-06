package kore.backend.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationOutboxService {

    private final NotificationOutboxRepository repository;
    private final ObjectMapper objectMapper;

    public void enqueue(NotificationMessage message, String routingKey) {
        if (repository.existsByEventId(message.eventId())) {
            return;
        }

        try {
            repository.save(new NotificationOutboxMessage(
                    message.eventId(),
                    routingKey,
                    objectMapper.writeValueAsString(message)));
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Não foi possível serializar evento de notificação", exception);
        } catch (DataIntegrityViolationException exception) {
            if (!repository.existsByEventId(message.eventId())) {
                throw exception;
            }
        }
    }
}
