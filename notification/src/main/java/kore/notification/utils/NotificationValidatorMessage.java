package kore.notification.utils;

public class NotificationValidatorMessage implements IValidator<String> {

    @Override
    public void validate(String message) {
        if (message == null || message.isEmpty()) {
            throw new IllegalArgumentException("Notification message cannot be null or empty");
        }
    }

}
