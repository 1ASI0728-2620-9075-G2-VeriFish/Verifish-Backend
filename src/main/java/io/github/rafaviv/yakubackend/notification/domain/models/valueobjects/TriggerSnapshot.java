package io.github.rafaviv.yakubackend.notification.domain.models.valueobjects;

import java.math.BigDecimal;

public record TriggerSnapshot(
    BigDecimal value,
    SensorType sensorType,
    String hardwareStatus
) {
}
