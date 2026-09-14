package io.github.rafaviv.yakubackend.notification.interfaces.rest.transform;

import io.github.rafaviv.yakubackend.notification.domain.models.commands.SendNotificationCommand;
import io.github.rafaviv.yakubackend.notification.domain.models.aggregates.Notification;
import io.github.rafaviv.yakubackend.notification.domain.models.valueobjects.NotificationType;
import io.github.rafaviv.yakubackend.notification.interfaces.rest.resources.NotificationResponseResource;
import io.github.rafaviv.yakubackend.notification.interfaces.rest.resources.SendNotificationRequestResource;

public class NotificationResourceMapper {

    public static SendNotificationCommand toCommand(SendNotificationRequestResource resource) {
        return new SendNotificationCommand(
            NotificationType.valueOf(resource.type().toUpperCase()),
            resource.message(),
            resource.userId(),
            resource.value(),
            resource.sensorType(),
            resource.hardwareStatus()
        );
    }

    public static NotificationResponseResource toResource(Notification notification) {
        return new NotificationResponseResource(
            notification.getId(),
            notification.getType().name(),
            notification.getMessage(),
            notification.getRecipient().userId(),
            notification.getTriggerData().value(),
            notification.getTriggerData().sensorType() != null ? notification.getTriggerData().sensorType().name() : null,
            notification.getTriggerData().hardwareStatus(),
            notification.getCreatedAt()
        );
    }
}
