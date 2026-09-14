package io.github.rafaviv.yakubackend.notification.domain.services;

import io.github.rafaviv.yakubackend.notification.domain.models.aggregates.Notification;
import java.util.List;
import java.util.Optional;

public interface NotificationRepository {
    Notification save(Notification notification);
    Optional<Notification> findById(Long id);
    List<Notification> findByRecipientUserId(Long userId);
}
