package io.github.rafaviv.yakubackend.notification.domain.services;

import io.github.rafaviv.yakubackend.notification.domain.models.aggregates.DeviceToken;
import java.util.List;
import java.util.Optional;

public interface DeviceTokenRepository {
    DeviceToken save(DeviceToken deviceToken);
    Optional<DeviceToken> findByUserIdAndFcmToken(Long userId, String fcmToken);
    List<DeviceToken> findByUserId(Long userId);
}
