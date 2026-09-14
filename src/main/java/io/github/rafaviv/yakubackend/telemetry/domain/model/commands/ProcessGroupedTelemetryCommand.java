package io.github.rafaviv.yakubackend.telemetry.domain.model.commands;

public record ProcessGroupedTelemetryCommand(
        String deviceId,
        Double temperature,
        Double turbidity,
        Double ica
) {
}
