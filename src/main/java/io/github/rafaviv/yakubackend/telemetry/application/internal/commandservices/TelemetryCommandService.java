package io.github.rafaviv.yakubackend.telemetry.application.internal.commandservices;

import io.github.rafaviv.yakubackend.telemetry.domain.model.commands.GenerateAggregatesCommand;
import io.github.rafaviv.yakubackend.telemetry.domain.model.commands.ProcessGroupedTelemetryCommand;

public interface TelemetryCommandService {
    void handle(ProcessGroupedTelemetryCommand command);

    void handle(GenerateAggregatesCommand command);

    Long handle(io.github.rafaviv.yakubackend.telemetry.domain.model.commands.ConfigureThresholdCommand command);
}
