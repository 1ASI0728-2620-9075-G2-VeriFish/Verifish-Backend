package io.github.rafaviv.yakubackend.telemetry.interfaces.rest;

import java.time.LocalDateTime;

public record SensorReadingResource(
        Long id,
        Long pondId,
        String sensorType,
        Double value,
        String unit,
        LocalDateTime timestamp
) {}