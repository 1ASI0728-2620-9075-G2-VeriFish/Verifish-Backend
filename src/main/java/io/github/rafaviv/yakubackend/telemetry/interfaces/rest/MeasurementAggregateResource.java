package io.github.rafaviv.yakubackend.telemetry.interfaces.rest;

import java.time.LocalDateTime;

public record MeasurementAggregateResource(
        Long id,
        Long pondId,
        String sensorType,
        Double minValue,
        Double maxValue,
        Double averageValue,
        LocalDateTime periodStart,
        LocalDateTime periodEnd
) {}