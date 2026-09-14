package kore.notification.application.useCases;

import org.springframework.stereotype.Service;

import kore.notification.application.repositories.INotificationRepository;
import kore.notification.entities.Notification;
import kore.notification.entities.valueObject.NotificationLevel;
import kore.notification.entities.valueObject.NotificationType;
import kore.notification.utils.IValidator;

@Service
public class EmitNotificationUseCase {

    private final INotificationRepository notificationRepository;

    private final IValidator<String> notificationValidatorLevel;

    private final IValidator<String> notificationValidatorType;

    private final IValidator<String> notificationValidatorMessage;

    public EmitNotificationUseCase(INotificationRepository notificationRepository,
            IValidator<String> notificationValidatorLevel, IValidator<String> notificationValidatorType,
            IValidator<String> notificationValidatorMessage) {
        this.notificationRepository = notificationRepository;
        this.notificationValidatorLevel = notificationValidatorLevel;
        this.notificationValidatorType = notificationValidatorType;
        this.notificationValidatorMessage = notificationValidatorMessage;
    }

    public NotificationResponse execute(
            NotificationRequest request) {

        notificationValidatorLevel.validate(request.level());
        notificationValidatorType.validate(request.type());
        notificationValidatorMessage.validate(request.message());

        Notification notification = new Notification(
                request.title(),
                request.message(),
                NotificationType.valueOf(request.type()),
                NotificationLevel.valueOf(request.level()));

        notification = notificationRepository.save(notification);

        return new NotificationResponse(
                notification.getId(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getType().name(),
                notification.getLevel().name());
    }

    public static record NotificationRequest(String title, String message, String type, String level) {
    }

    public static record NotificationResponse(Long id, String title, String message, String type, String level) {

    }

}
