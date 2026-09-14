package io.github.rafaviv.yakubackend.telemetry.application.internal.queryservices;

import io.github.rafaviv.yakubackend.telemetry.domain.model.aggregates.MeasurementAggregate;
import io.github.rafaviv.yakubackend.telemetry.domain.model.aggregates.SensorReading;
import io.github.rafaviv.yakubackend.telemetry.domain.model.queries.GetHistoricalDataQuery;
import io.github.rafaviv.yakubackend.telemetry.domain.model.queries.GetPondStatusQuery;

import java.util.List;

public interface TelemetryQueryService {
    List<SensorReading> handle(GetPondStatusQuery query);
    List<MeasurementAggregate> handle(GetHistoricalDataQuery query);
    java.util.Optional<io.github.rafaviv.yakubackend.telemetry.domain.model.aggregates.Threshold> handle(io.github.rafaviv.yakubackend.telemetry.domain.model.queries.GetThresholdBySpeciesQuery query);
}
