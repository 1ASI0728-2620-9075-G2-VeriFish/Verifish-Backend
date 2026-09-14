package io.github.rafaviv.yakubackend.subscription.infrastructure.persistence.jpa.repositories;

import io.github.rafaviv.yakubackend.subscription.domain.model.entities.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
}
