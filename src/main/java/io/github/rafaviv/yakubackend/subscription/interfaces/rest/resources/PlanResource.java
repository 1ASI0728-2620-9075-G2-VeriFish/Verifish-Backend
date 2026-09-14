package io.github.rafaviv.yakubackend.subscription.interfaces.rest.resources;

public record PlanResource(Long id, String name, Double price, String currency, Integer maxPonds,
                Integer durationInDays, String stripePriceId) {
}
