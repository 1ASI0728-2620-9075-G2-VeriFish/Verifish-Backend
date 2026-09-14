package io.github.rafaviv.yakubackend.telemetry.interfaces.rest.transform;

import io.github.rafaviv.yakubackend.telemetry.domain.model.commands.ConfigureThresholdCommand;
import io.github.rafaviv.yakubackend.telemetry.interfaces.rest.resources.ConfigureThresholdResource;

public class ConfigureThresholdCommandFromResourceAssembler {
    public static ConfigureThresholdCommand toCommandFromResource(ConfigureThresholdResource resource) {
        return new ConfigureThresholdCommand(
                resource.species(),
                resource.minTemperature(),
                resource.maxTemperature(),
                resource.minTurbidity(),
                resource.maxTurbidity()
        );
    }
}
