package kore.notification.entities;

import java.time.LocalDateTime;

import kore.notification.entities.valueObject.NotificationLevel;
import kore.notification.entities.valueObject.NotificationType;

public class Notification {
    private Long id;
    private String title;
    private String message;
    private boolean read = false;
    private String sourceEventId;
    private NotificationType type;
    private NotificationLevel level;
    private LocalDateTime createdAt;

    public Notification() {
    }

    public Notification(String title, String message, NotificationType type, NotificationLevel level) {
        this(title, message, type, level, null);
    }

    public Notification(String title, String message, NotificationType type, NotificationLevel level,
            String sourceEventId) {
        this.title = title;
        this.message = message;
        this.type = type;
        this.level = level;
        this.read = false;
        this.sourceEventId = sourceEventId;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public String getSourceEventId() {
        return sourceEventId;
    }

    public void setSourceEventId(String sourceEventId) {
        this.sourceEventId = sourceEventId;
    }

    public NotificationType getType() {
        return type;
    }

    public void setType(NotificationType type) {
        this.type = type;
    }

    public NotificationLevel getLevel() {
        return level;
    }

    public void setLevel(NotificationLevel level) {
        this.level = level;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

}
