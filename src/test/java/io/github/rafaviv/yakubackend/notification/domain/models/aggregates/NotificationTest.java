package io.github.rafaviv.yakubackend.notification.domain.models.aggregates;

import io.github.rafaviv.yakubackend.notification.domain.models.valueobjects.NotificationType;
import io.github.rafaviv.yakubackend.notification.domain.models.valueobjects.RecipientInfo;
import io.github.rafaviv.yakubackend.notification.domain.models.valueobjects.SensorType;
import io.github.rafaviv.yakubackend.notification.domain.models.valueobjects.TriggerSnapshot;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class NotificationTest {

    @Test
    @DisplayName("Given valid notification data, When creating notification, Then it is created successfully")
    void createNotification_Successfully() {
        RecipientInfo recipient = new RecipientInfo(1L);
        TriggerSnapshot trigger = new TriggerSnapshot(new BigDecimal("25.5"), SensorType.TEMPERATURE, "OK");

        Notification notification = new Notification(
            NotificationType.WARNING,
            "Temperature is high",
            recipient,
            trigger
        );

        assertEquals(NotificationType.WARNING, notification.getType());
        assertEquals("Temperature is high", notification.getMessage());
        assertEquals(recipient, notification.getRecipient());
        assertEquals(trigger, notification.getTriggerData());
        assertNotNull(notification.getCreatedAt());
        assertNull(notification.getId());
    }

    @Test
    @DisplayName("Given notification, When setting ID, Then ID is set")
    void setId_Successfully() {
        RecipientInfo recipient = new RecipientInfo(1L);
        TriggerSnapshot trigger = new TriggerSnapshot(null, null, null);
        Notification notification = new Notification(NotificationType.INFO, "Test message", recipient, trigger);

        notification.setId(100L);

        assertEquals(100L, notification.getId());
    }

    @Test
    @DisplayName("Given notification with ID, When setting ID again, Then throws IllegalStateException")
    void setId_AlreadySet_ThrowsException() {
        RecipientInfo recipient = new RecipientInfo(1L);
        TriggerSnapshot trigger = new TriggerSnapshot(null, null, null);
        Notification notification = new Notification(NotificationType.INFO, "Test message", recipient, trigger);
        notification.setId(100L);

        assertThrows(IllegalStateException.class, () -> notification.setId(200L));
    }

    @Test
    @DisplayName("Given null type, When creating notification, Then throws IllegalArgumentException")
    void createNotification_NullType_ThrowsException() {
        RecipientInfo recipient = new RecipientInfo(1L);
        TriggerSnapshot trigger = new TriggerSnapshot(null, null, null);

        assertThrows(IllegalArgumentException.class, () ->
            new Notification(null, "Test message", recipient, trigger));
    }

    @Test
    @DisplayName("Given null message, When creating notification, Then throws IllegalArgumentException")
    void createNotification_NullMessage_ThrowsException() {
        RecipientInfo recipient = new RecipientInfo(1L);
        TriggerSnapshot trigger = new TriggerSnapshot(null, null, null);

        assertThrows(IllegalArgumentException.class, () ->
            new Notification(NotificationType.INFO, null, recipient, trigger));
    }

    @Test
    @DisplayName("Given blank message, When creating notification, Then throws IllegalArgumentException")
    void createNotification_BlankMessage_ThrowsException() {
        RecipientInfo recipient = new RecipientInfo(1L);
        TriggerSnapshot trigger = new TriggerSnapshot(null, null, null);

        assertThrows(IllegalArgumentException.class, () ->
            new Notification(NotificationType.INFO, "   ", recipient, trigger));
    }

    @Test
    @DisplayName("Given null recipient, When creating notification, Then throws IllegalArgumentException")
    void createNotification_NullRecipient_ThrowsException() {
        TriggerSnapshot trigger = new TriggerSnapshot(null, null, null);

        assertThrows(IllegalArgumentException.class, () ->
            new Notification(NotificationType.INFO, "Test message", null, trigger));
    }

    @Test
    @DisplayName("Given notification, When creating, Then createdAt is set to current time")
    void createNotification_CreatedAtIsSet() {
        RecipientInfo recipient = new RecipientInfo(1L);
        TriggerSnapshot trigger = new TriggerSnapshot(null, null, null);
        LocalDateTime before = LocalDateTime.now().minusSeconds(1);

        Notification notification = new Notification(NotificationType.INFO, "Test", recipient, trigger);

        assertTrue(notification.getCreatedAt().isAfter(before));
        assertTrue(notification.getCreatedAt().isBefore(LocalDateTime.now().plusSeconds(1)));
    }

    @Test
    @DisplayName("Given notification with null trigger data, When creating, Then it is allowed")
    void createNotification_NullTriggerData_Allowed() {
        RecipientInfo recipient = new RecipientInfo(1L);

        Notification notification = new Notification(NotificationType.INFO, "Test", recipient, null);

        assertNull(notification.getTriggerData());
    }
}
