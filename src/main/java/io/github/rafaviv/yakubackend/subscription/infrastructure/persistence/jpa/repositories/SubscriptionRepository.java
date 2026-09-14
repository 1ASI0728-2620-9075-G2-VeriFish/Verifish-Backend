package io.github.rafaviv.yakubackend.subscription.infrastructure.persistence.jpa.repositories;

import io.github.rafaviv.yakubackend.subscription.domain.model.aggregates.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    Optional<Subscription> findByUserId(Long userId);
}
