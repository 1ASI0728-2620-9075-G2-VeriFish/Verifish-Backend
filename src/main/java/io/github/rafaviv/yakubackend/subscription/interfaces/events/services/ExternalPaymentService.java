package io.github.rafaviv.yakubackend.subscription.interfaces.events.services;

import io.github.rafaviv.yakubackend.subscription.domain.model.valueobjects.Currency;
import io.github.rafaviv.yakubackend.subscription.domain.model.valueobjects.PaymentStatus;

public interface ExternalPaymentService {
    PaymentStatus processPayment(Long userId, Double amount, Currency currency, String paymentMethodId);
}



