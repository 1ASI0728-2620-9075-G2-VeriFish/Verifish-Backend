package io.github.rafaviv.yakubackend.subscription.domain.ports;

import io.github.rafaviv.yakubackend.subscription.domain.model.valueobjects.Currency;
import io.github.rafaviv.yakubackend.subscription.domain.model.valueobjects.PaymentStatus;

/**
 * Puerto de salida (Interface) del Dominio.
 * Define qué necesitamos del pago sin mencionar a Stripe.
 */
public interface ExternalPaymentService {
    PaymentStatus processPayment(Long userId, Double amount, Currency currency, String paymentMethodId);


}