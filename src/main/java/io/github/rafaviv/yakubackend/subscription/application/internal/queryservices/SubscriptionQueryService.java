package io.github.rafaviv.yakubackend.subscription.application.internal.queryservices;

import io.github.rafaviv.yakubackend.subscription.domain.model.aggregates.Subscription;
import io.github.rafaviv.yakubackend.subscription.domain.model.entities.Plan;

import java.util.List;
import java.util.Optional;

public interface SubscriptionQueryService {
    Optional<Subscription> getUserSubscriptionStatus(Long userId);

    List<Plan> getAvailablePlans();
    Optional<Plan> getPlanById(Long planId);
}
