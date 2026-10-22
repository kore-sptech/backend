package kore.backend.messaging;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationOutboxRepository extends JpaRepository<NotificationOutboxMessage, Long> {

    boolean existsByEventId(String eventId);

    List<NotificationOutboxMessage> findByPublishedAtIsNullOrderByCreatedAtAsc(Pageable pageable);
}
