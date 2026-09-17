package kore.notification.adapters.repositories;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import kore.notification.adapters.entities.JpaNotificationEntity;
import kore.notification.entities.Notification;

@Component
public class NotificationMapper implements IMapper<Notification, JpaNotificationEntity> {

    private final ModelMapper modelMapper;

    public NotificationMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    @Override
    public Notification toDomain(JpaNotificationEntity infrastructure) {
        return modelMapper.map(infrastructure, Notification.class);
    }

    @Override
    public JpaNotificationEntity toInfrastructure(Notification domain) {
        return modelMapper.map(domain, JpaNotificationEntity.class);
    }

}
