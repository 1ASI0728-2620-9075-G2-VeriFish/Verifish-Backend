package io.github.rafaviv.yakubackend.iam.domain.model.aggregates;

import io.github.rafaviv.yakubackend.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.*;
import lombok.Getter;

import java.util.UUID;

@Entity
@Getter
@Table(name = "farm_tokens")
public class FarmToken extends AuditableAbstractAggregateRoot<FarmToken> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(nullable = false)
    private Long farmId;

    @Column(nullable = false)
    private boolean isUsed;

    public FarmToken() {
    }

    public FarmToken(Long farmId) {
        this.token = UUID.randomUUID().toString();
        this.farmId = farmId;
        this.isUsed = false;
    }

    public void markAsUsed() {
        if (this.isUsed) {
            throw new IllegalStateException("Token is already used");
        }
        this.isUsed = true;
    }
}
