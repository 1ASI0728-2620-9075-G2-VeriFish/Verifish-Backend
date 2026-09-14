package io.github.rafaviv.yakubackend.notification.domain.services;

import io.github.rafaviv.yakubackend.notification.domain.models.aggregates.Notification;
import java.util.List;

public interface PushNotificationService {
    void sendNotification(Notification notification, List<String> fcmTokens);
}
