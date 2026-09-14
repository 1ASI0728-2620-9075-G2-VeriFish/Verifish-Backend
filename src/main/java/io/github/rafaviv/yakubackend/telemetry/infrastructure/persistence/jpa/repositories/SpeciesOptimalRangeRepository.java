package io.github.rafaviv.yakubackend.telemetry.infrastructure.persistence.jpa.repositories;

import io.github.rafaviv.yakubackend.telemetry.domain.model.aggregates.SpeciesOptimalRange;
import io.github.rafaviv.yakubackend.telemetry.domain.model.valueobjects.SensorType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SpeciesOptimalRangeRepository extends JpaRepository<SpeciesOptimalRange, Long> {
    Optional<SpeciesOptimalRange> findByPondIdAndSensorType(Long pondId, SensorType sensorType);
}
