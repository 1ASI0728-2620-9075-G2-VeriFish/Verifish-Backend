package io.github.rafaviv.yakubackend.telemetry.infrastructure.configuration.infrastructure.persistence.jpa.seeding;

import io.github.rafaviv.yakubackend.telemetry.domain.model.aggregates.Threshold;
import io.github.rafaviv.yakubackend.telemetry.domain.model.valueobjects.Species;
import io.github.rafaviv.yakubackend.telemetry.infrastructure.persistence.jpa.repositories.ThresholdRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(2)
public class ThresholdSeeder implements CommandLineRunner {
    private final ThresholdRepository thresholdRepository;

    public ThresholdSeeder(ThresholdRepository thresholdRepository) {
        this.thresholdRepository = thresholdRepository;
    }

    @Override
    public void run(String... args) {
        if (!thresholdRepository.existsBySpecies(Species.TRUCHA)) {
            thresholdRepository.save(new Threshold(Species.TRUCHA, 10.0, 18.0, 3.5, 4.5));
            System.out.println("Seeded TRUCHA thresholds.");
        }
        if (!thresholdRepository.existsBySpecies(Species.PAICHE)) {
            thresholdRepository.save(new Threshold(Species.PAICHE, 25.0, 32.0, 2.5, 3.8));
            System.out.println("Seeded PAICHE thresholds.");
        }
        if (!thresholdRepository.existsBySpecies(Species.TILAPIA)) {
            thresholdRepository.save(new Threshold(Species.TILAPIA, 20.0, 30.0, 3.0, 4.0));
            System.out.println("Seeded TILAPIA thresholds.");
        }
    }
}
