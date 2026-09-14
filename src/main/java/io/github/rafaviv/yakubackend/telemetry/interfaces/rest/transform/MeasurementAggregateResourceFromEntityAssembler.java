package io.github.rafaviv.yakubackend.telemetry.interfaces.rest.transform;

import io.github.rafaviv.yakubackend.telemetry.domain.model.aggregates.MeasurementAggregate;
import io.github.rafaviv.yakubackend.telemetry.interfaces.rest.MeasurementAggregateResource;

public class MeasurementAggregateResourceFromEntityAssembler {
    public static MeasurementAggregateResource toResourceFromEntity(MeasurementAggregate entity) {
        return new MeasurementAggregateResource(
                entity.getId(),
                entity.getPondId(),
                entity.getSensorType().name(),
                entity.getMinValue(),
                entity.getMaxValue(),
                entity.getAverageValue(),
                entity.getPeriodStart(),
                entity.getPeriodEnd()
        );
    }
}