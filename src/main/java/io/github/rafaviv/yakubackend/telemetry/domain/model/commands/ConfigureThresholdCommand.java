package io.github.rafaviv.yakubackend.telemetry.domain.model.commands;

public record ConfigureThresholdCommand(
        String species,
        Double minTemperature,
        Double maxTemperature,
        Double minTurbidity,
        Double maxTurbidity
) {
}
