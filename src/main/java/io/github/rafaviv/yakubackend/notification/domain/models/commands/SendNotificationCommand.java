package io.github.rafaviv.yakubackend.notification.domain.models.commands;

import java.math.BigDecimal;
import io.github.rafaviv.yakubackend.notification.domain.models.valueobjects.NotificationType;

public record SendNotificationCommand(
    NotificationType type,
    String message,
    Long userId,
    BigDecimal value,
    String sensorType,
    String hardwareStatus
) {}
