package kore.notification.consumer;

import kore.notification.application.repositories.INotificationRepository;
import kore.notification.application.useCases.EmitNotificationUseCase;
import kore.notification.application.useCases.EmitNotificationUseCase.NotificationRequest;
import kore.notification.messaging.NotificationMessage;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NotificationConsumerTest {

    private final EmitNotificationUseCase useCase = mock(EmitNotificationUseCase.class);
    private final INotificationRepository repository = mock(INotificationRepository.class);
    private final NotificationConsumer consumer = new NotificationConsumer(useCase, repository);

    @Test
    void persistsEventWithItsIdAsIdempotencyKey() {
        NotificationMessage event = appointmentEvent();
        when(repository.existsBySourceEventId("appointment-upcoming:42")).thenReturn(false);

        consumer.receiveMessage(event);

        ArgumentCaptor<NotificationRequest> request = ArgumentCaptor.forClass(NotificationRequest.class);
        verify(useCase).execute(request.capture());
        org.junit.jupiter.api.Assertions.assertEquals("appointment-upcoming:42", request.getValue().sourceEventId());
    }

    @Test
    void ignoresAlreadyPersistedEvent() {
        when(repository.existsBySourceEventId("appointment-upcoming:42")).thenReturn(true);

        consumer.receiveMessage(appointmentEvent());

        verify(useCase, never()).execute(any());
    }

    @Test
    void rejectsUnsupportedEventType() {
        NotificationMessage invalid = new NotificationMessage(
                "unknown:1", 1, "UNKNOWN", "Title", "Message", "STOCK", "WARNING",
                null, null, null, 2L, 3L, "Product", 1, 2);

        assertThrows(IllegalArgumentException.class, () -> consumer.receiveMessage(invalid));
        verify(useCase, never()).execute(any());
    }

    private static NotificationMessage appointmentEvent() {
        return new NotificationMessage(
                "appointment-upcoming:42", 1, "APPOINTMENT_UPCOMING", "Agendamento próximo",
                "O agendamento está próximo.", "APPOINTMENT", "INFO",
                42L, "Cliente", null, null, 3L, null, null, null);
    }
}
