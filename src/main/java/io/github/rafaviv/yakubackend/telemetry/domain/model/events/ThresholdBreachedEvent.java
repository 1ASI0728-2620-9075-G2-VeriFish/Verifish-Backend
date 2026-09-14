package io.github.rafaviv.yakubackend.telemetry.domain.model.events;

public record ThresholdBreachedEvent(
        Long pondId,
        Long targetUserId,
        String severity,
        String message
) {
}
