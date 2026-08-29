package kore.notification.utils;

import org.springframework.stereotype.Component;

@Component
public class NotificationValidatorMessage implements IValidator<String> {

    @Override
    public void validate(String message) {
        if (message == null || message.isEmpty()) {
            throw new IllegalArgumentException("Notification message cannot be null or empty");
        }
    }

}
