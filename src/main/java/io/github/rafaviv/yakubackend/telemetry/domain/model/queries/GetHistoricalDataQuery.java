package io.github.rafaviv.yakubackend.telemetry.domain.model.queries;

import io.github.rafaviv.yakubackend.telemetry.domain.model.valueobjects.TimeFilter;

public record GetHistoricalDataQuery(
        Long pondId,
        TimeFilter timeFilter
) {
}
