package kore.notification.adapters.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import kore.notification.adapters.entities.JpaNotificationEntity;

public interface JpaNotificationRepository extends JpaRepository<JpaNotificationEntity, Long> {

}
