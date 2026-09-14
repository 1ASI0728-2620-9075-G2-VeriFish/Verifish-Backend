package io.github.rafaviv.yakubackend.subscription.infrastructure.adapters.stripe;

import io.github.rafaviv.yakubackend.subscription.domain.ports.ExternalPaymentService;
import io.github.rafaviv.yakubackend.subscription.domain.model.valueobjects.Currency;
import io.github.rafaviv.yakubackend.subscription.domain.model.valueobjects.PaymentStatus;

import org.springframework.stereotype.Service;

/**
 * Patrón: Adapter / Anti-Corruption Layer (ACL)
 * Si Stripe cambia su API, solo modificamos este archivo.
 */
@Service
public class StripePaymentAdapterACL implements ExternalPaymentService {

    @Override
    public PaymentStatus processPayment(Long userId, Double amount, Currency currency, String paymentMethodId) {

        System.out.println("[STRIPE ACL] Procesando pago de " + amount + " " + currency + " para el usuario " + userId);

        return PaymentStatus.SUCCESS;
    }
}

