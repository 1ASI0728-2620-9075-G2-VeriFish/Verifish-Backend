package io.github.rafaviv.yakubackend.telemetry.interfaces.rest.transform;

import io.github.rafaviv.yakubackend.telemetry.domain.model.aggregates.Threshold;
import io.github.rafaviv.yakubackend.telemetry.interfaces.rest.resources.ThresholdResource;

public class ThresholdResourceFromEntityAssembler {
    public static ThresholdResource toResourceFromEntity(Threshold entity) {
        return new ThresholdResource(
                entity.getId(),
                entity.getSpecies() != null ? entity.getSpecies().name() : null,
                entity.getMinTemperature(),
                entity.getMaxTemperature(),
                entity.getMinTurbidity(),
                entity.getMaxTurbidity()
        );
    }
}
