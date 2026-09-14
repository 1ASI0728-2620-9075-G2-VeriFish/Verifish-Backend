package io.github.rafaviv.yakubackend.notification.domain.models.commands;

public record RegisterDeviceTokenCommand(Long userId, String fcmToken) {}
