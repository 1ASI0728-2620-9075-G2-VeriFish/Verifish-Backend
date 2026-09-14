package io.github.rafaviv.yakubackend.telemetry.interfaces.rest.transform;

import io.github.rafaviv.yakubackend.telemetry.domain.model.aggregates.SensorReading;
import io.github.rafaviv.yakubackend.telemetry.interfaces.rest.SensorReadingResource;

public class SensorReadingResourceFromEntityAssembler {
    public static SensorReadingResource toResourceFromEntity(SensorReading entity) {
        return new SensorReadingResource(
                entity.getId(),
                entity.getPondId(),
                entity.getSensorType().name(),
                entity.getMeasurement().getValue(),
                entity.getMeasurement().getUnit(),
                entity.getTimestamp()
        );
    }
}