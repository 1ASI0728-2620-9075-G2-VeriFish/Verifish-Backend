package io.github.rafaviv.yakubackend.notification.domain.models.valueobjects;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RecipientInfoTest {

    @Test
    @DisplayName("Given valid user ID, When creating RecipientInfo, Then it is created successfully")
    void createRecipientInfo_Successfully() {
        RecipientInfo info = new RecipientInfo(1L);

        assertEquals(1L, info.userId());
    }

    @Test
    @DisplayName("Given null user ID, When creating RecipientInfo, Then throws IllegalArgumentException")
    void createRecipientInfo_NullUserId_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> new RecipientInfo(null));
    }

    @Test
    @DisplayName("Given zero user ID, When creating RecipientInfo, Then throws IllegalArgumentException")
    void createRecipientInfo_ZeroUserId_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> new RecipientInfo(0L));
    }

    @Test
    @DisplayName("Given negative user ID, When creating RecipientInfo, Then throws IllegalArgumentException")
    void createRecipientInfo_NegativeUserId_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> new RecipientInfo(-1L));
    }
}
