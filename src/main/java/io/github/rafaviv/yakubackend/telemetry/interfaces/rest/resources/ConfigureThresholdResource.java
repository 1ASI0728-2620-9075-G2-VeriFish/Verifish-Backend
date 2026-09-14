package io.github.rafaviv.yakubackend.telemetry.interfaces.rest.resources;

public record ConfigureThresholdResource(
        String species,
        Double minTemperature,
        Double maxTemperature,
        Double minTurbidity,
        Double maxTurbidity
) {
}
