package kore.notification.entities.valueObject;

public enum NotificationType {
    APPOIMENTMENT,
    PAYMENT,
    STOCK;

    public static NotificationType fromString(String type) {
        for (NotificationType notificationType : NotificationType.values()) {
            if (notificationType.name().equalsIgnoreCase(type)) {
                return notificationType;
            }
        }
        throw new IllegalArgumentException("Invalid notification type: " + type);
    }

    public static void isValidType(String type) {
        for (NotificationType notificationType : NotificationType.values()) {
            if (notificationType.name().equalsIgnoreCase(type)) {
                return;
            }
        }
        throw new IllegalArgumentException("Invalid notification type: " + type);
    }

}
