package kore.notification.utils;

import kore.notification.entities.valueObject.NotificationType;

import org.springframework.stereotype.Component;

@Component
public class NotificationValidatorType implements IValidator<String> {

    @Override
    public void validate(String type) {
        if (type == null || type.isEmpty()) {
            throw new IllegalArgumentException("Notification type cannot be null or empty");
        }

        NotificationType.isValidType(type);
    }

}
