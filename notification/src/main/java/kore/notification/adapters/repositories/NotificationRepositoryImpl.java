package kore.notification.adapters.repositories;

import org.springframework.stereotype.Repository;

import kore.notification.adapters.entities.JpaNotificationEntity;
import kore.notification.application.repositories.INotificationRepository;
import kore.notification.entities.Notification;

@Repository
public class NotificationRepositoryImpl implements INotificationRepository {

    private final JpaNotificationRepository jpaNotificationRepository;

    private final NotificationMapper notificationMapper;

    public NotificationRepositoryImpl(JpaNotificationRepository jpaNotificationRepository,
            NotificationMapper notificationMapper) {
        this.jpaNotificationRepository = jpaNotificationRepository;
        this.notificationMapper = notificationMapper;
    }

    @Override
    public Notification save(Notification entity) {
        JpaNotificationEntity jpaNotificationEntity = jpaNotificationRepository
                .save(notificationMapper.toInfrastructure(entity));

        return notificationMapper.toDomain(jpaNotificationEntity);
    }

}
