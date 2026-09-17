package kore.notification.entities.valueObject;

public enum NotificationLevel {
    INFO,
    WARNING,
    CRITICAL;

    public static NotificationLevel fromString(String level) {
        for (NotificationLevel notificationLevel : NotificationLevel.values()) {
            if (notificationLevel.name().equalsIgnoreCase(level)) {
                return notificationLevel;
            }
        }
        throw new IllegalArgumentException("Invalid notification level: " + level);
    }

    public static void isValidLevel(String level) {
        for (NotificationLevel notificationLevel : NotificationLevel.values()) {
            if (notificationLevel.name().equalsIgnoreCase(level)) {
                return;
            }
        }
        throw new IllegalArgumentException("Invalid notification level: " + level);
    }
}
