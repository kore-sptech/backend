package kore.backend.messaging;

import java.time.LocalDateTime;

public record NotificationMessage(
        String eventId,
        int eventVersion,
        String eventType,
        String title,
        String message,
        String type,
        String level,
        Long appointmentId,
        String clientName,
        LocalDateTime appointmentStart,
        Long productId,
        Long userId,
        String productName,
        Integer availableQuantity,
        Integer threshold,
        String severidade,
        String entidadeTipo,
        Long entidadeId,
        String acaoTipo) {
}
