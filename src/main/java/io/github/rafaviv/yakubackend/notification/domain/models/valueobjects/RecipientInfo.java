package io.github.rafaviv.yakubackend.notification.domain.models.valueobjects;

public record RecipientInfo(
    Long userId
) {
    public RecipientInfo {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("User ID must be valid");
        }
    }
}
