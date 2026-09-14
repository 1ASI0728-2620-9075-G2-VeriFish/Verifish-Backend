package io.github.rafaviv.yakubackend.telemetry.application.internal.queryservices;

import io.github.rafaviv.yakubackend.telemetry.domain.model.aggregates.MeasurementAggregate;
import io.github.rafaviv.yakubackend.telemetry.domain.model.aggregates.SensorReading;
import io.github.rafaviv.yakubackend.telemetry.domain.model.queries.GetHistoricalDataQuery;
import io.github.rafaviv.yakubackend.telemetry.domain.model.queries.GetPondStatusQuery;
import io.github.rafaviv.yakubackend.telemetry.infrastructure.persistence.jpa.repositories.MeasurementAggregateRepository;
import io.github.rafaviv.yakubackend.telemetry.infrastructure.persistence.jpa.repositories.SensorReadingRepository;
import io.github.rafaviv.yakubackend.telemetry.domain.model.valueobjects.Species;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TelemetryQueryServiceImpl implements TelemetryQueryService {

    private final SensorReadingRepository sensorReadingRepository;
    private final MeasurementAggregateRepository measurementAggregateRepository;
    private final io.github.rafaviv.yakubackend.telemetry.infrastructure.persistence.jpa.repositories.ThresholdRepository thresholdRepository;

    public TelemetryQueryServiceImpl(SensorReadingRepository sensorReadingRepository,
                                     MeasurementAggregateRepository measurementAggregateRepository,
                                     io.github.rafaviv.yakubackend.telemetry.infrastructure.persistence.jpa.repositories.ThresholdRepository thresholdRepository) {
        this.sensorReadingRepository = sensorReadingRepository;
        this.measurementAggregateRepository = measurementAggregateRepository;
        this.thresholdRepository = thresholdRepository;
    }

    @Override
    public List<SensorReading> handle(GetPondStatusQuery query) {
        return sensorReadingRepository.findByPondIdOrderByTimestampDesc(query.pondId());
    }

    @Override
    public List<MeasurementAggregate> handle(GetHistoricalDataQuery query) {
        LocalDateTime start = switch (query.timeFilter()) {
            case DAILY -> LocalDateTime.now().minusDays(1);
            case WEEKLY -> LocalDateTime.now().minusWeeks(1);
            case MONTHLY -> LocalDateTime.now().minusMonths(1);
            case YEARLY -> LocalDateTime.now().minusYears(1);
        };
        return measurementAggregateRepository.findByPondIdAndPeriodStartGreaterThanEqual(query.pondId(), start);
    }

    @Override
    public java.util.Optional<io.github.rafaviv.yakubackend.telemetry.domain.model.aggregates.Threshold> handle(io.github.rafaviv.yakubackend.telemetry.domain.model.queries.GetThresholdBySpeciesQuery query) {
        return thresholdRepository.findBySpecies(Species.valueOf(query.species()));
    }
}
