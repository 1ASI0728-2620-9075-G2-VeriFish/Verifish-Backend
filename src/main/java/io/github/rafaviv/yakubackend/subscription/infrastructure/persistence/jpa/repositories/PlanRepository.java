package io.github.rafaviv.yakubackend.subscription.infrastructure.persistence.jpa.repositories;

import io.github.rafaviv.yakubackend.subscription.domain.model.entities.Plan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PlanRepository extends JpaRepository<Plan, Long> {
    Optional<Plan> findByName(String name);
}
