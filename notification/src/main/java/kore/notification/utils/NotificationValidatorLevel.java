package kore.notification.utils;

import kore.notification.entities.valueObject.NotificationLevel;

public class NotificationValidatorLevel implements IValidator<String> {

    @Override
    public void validate(String level) {
        if (level == null || level.isEmpty()) {
            throw new IllegalArgumentException("Notification level cannot be null or empty");
        }

        NotificationLevel.isValidLevel(level);
    }

}
