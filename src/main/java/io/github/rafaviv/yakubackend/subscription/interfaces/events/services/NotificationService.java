package io.github.rafaviv.yakubackend.subscription.interfaces.events.services;

public interface NotificationService {
    void sendNotification(Long userId, String message);
}
