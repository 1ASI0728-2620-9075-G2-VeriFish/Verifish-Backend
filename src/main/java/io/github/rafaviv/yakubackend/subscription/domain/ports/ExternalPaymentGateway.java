package io.github.rafaviv.yakubackend.subscription.domain.ports;

public interface ExternalPaymentGateway {
    String generatePaymentIntent(Long amountInCents, String currency);
    String createCheckoutSession(Long userId, io.github.rafaviv.yakubackend.subscription.domain.model.entities.Plan plan);
}
