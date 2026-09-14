package io.github.rafaviv.yakubackend.notification.domain.factories;

import io.github.rafaviv.yakubackend.notification.domain.models.aggregates.Notification;
import io.github.rafaviv.yakubackend.notification.domain.models.valueobjects.NotificationType;
import io.github.rafaviv.yakubackend.notification.domain.models.valueobjects.RecipientInfo;
import io.github.rafaviv.yakubackend.notification.domain.models.valueobjects.SensorType;
import io.github.rafaviv.yakubackend.notification.domain.models.valueobjects.TriggerSnapshot;

import java.math.BigDecimal;

/**
 * Pattern: Factory (Creational)
 * Centraliza la lógica de creación de notificaciones para evitar que la capa
 * de aplicación tenga que conocer los detalles de instanciación de los Value Objects internos.
 */
public class NotificationFactory {

    public static Notification createCriticalTelemetryAlert(
            Long userId, String message, Double currentValue, SensorType sensorType) {

        RecipientInfo recipient = new RecipientInfo(userId);
        TriggerSnapshot snapshot = new TriggerSnapshot(
                BigDecimal.valueOf(currentValue),
                sensorType,
                "CRITICAL_STATE"
        );

        return new Notification(NotificationType.CRITICAL, message, recipient, snapshot);
    }
}