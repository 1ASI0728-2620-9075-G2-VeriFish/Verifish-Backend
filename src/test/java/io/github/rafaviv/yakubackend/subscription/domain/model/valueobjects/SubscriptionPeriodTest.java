package io.github.rafaviv.yakubackend.subscription.domain.model.valueobjects;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class SubscriptionPeriodTest {

    @Test
    @DisplayName("Given valid dates, When creating period, Then it is created successfully")
    void createSubscriptionPeriod_Successfully() {
        LocalDate start = LocalDate.now();
        LocalDate end = start.plusDays(30);

        SubscriptionPeriod period = new SubscriptionPeriod(start, end);

        assertEquals(start, period.startDate());
        assertEquals(end, period.endDate());
    }

    @Test
    @DisplayName("Given null start date, When creating period, Then throws IllegalArgumentException")
    void createSubscriptionPeriod_NullStartDate_ThrowsException() {
        LocalDate end = LocalDate.now().plusDays(30);

        assertThrows(IllegalArgumentException.class, () -> new SubscriptionPeriod(null, end));
    }

    @Test
    @DisplayName("Given null end date, When creating period, Then throws IllegalArgumentException")
    void createSubscriptionPeriod_NullEndDate_ThrowsException() {
        LocalDate start = LocalDate.now();

        assertThrows(IllegalArgumentException.class, () -> new SubscriptionPeriod(start, null));
    }

    @Test
    @DisplayName("Given end date before start date, When creating period, Then throws IllegalArgumentException")
    void createSubscriptionPeriod_EndDateBeforeStartDate_ThrowsException() {
        LocalDate start = LocalDate.now();
        LocalDate end = start.minusDays(1);

        assertThrows(IllegalArgumentException.class, () -> new SubscriptionPeriod(start, end));
    }

    @Test
    @DisplayName("Given same start and end date, When creating period, Then it is created successfully")
    void createSubscriptionPeriod_SameStartAndEndDate_Successfully() {
        LocalDate date = LocalDate.now();

        SubscriptionPeriod period = new SubscriptionPeriod(date, date);

        assertEquals(date, period.startDate());
        assertEquals(date, period.endDate());
    }

    @Test
    @DisplayName("Given period in the future, When checking isActive, Then returns false")
    void isActive_FuturePeriod_ReturnsFalse() {
        LocalDate start = LocalDate.now().plusDays(10);
        LocalDate end = start.plusDays(30);

        SubscriptionPeriod period = new SubscriptionPeriod(start, end);

        assertFalse(period.isActive());
    }

    @Test
    @DisplayName("Given period in the past, When checking isActive, Then returns false")
    void isActive_PastPeriod_ReturnsFalse() {
        LocalDate end = LocalDate.now().minusDays(1);
        LocalDate start = end.minusDays(30);

        SubscriptionPeriod period = new SubscriptionPeriod(start, end);

        assertFalse(period.isActive());
    }

    @Test
    @DisplayName("Given current period, When checking isActive, Then returns true")
    void isActive_CurrentPeriod_ReturnsTrue() {
        LocalDate start = LocalDate.now().minusDays(10);
        LocalDate end = LocalDate.now().plusDays(10);

        SubscriptionPeriod period = new SubscriptionPeriod(start, end);

        assertTrue(period.isActive());
    }

    @Test
    @DisplayName("Given period starting today, When checking isActive, Then returns true")
    void isActive_StartsToday_ReturnsTrue() {
        LocalDate start = LocalDate.now();
        LocalDate end = start.plusDays(30);

        SubscriptionPeriod period = new SubscriptionPeriod(start, end);

        assertTrue(period.isActive());
    }

    @Test
    @DisplayName("Given period ending today, When checking isActive, Then returns true")
    void isActive_EndsToday_ReturnsTrue() {
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(30);

        SubscriptionPeriod period = new SubscriptionPeriod(start, end);

        assertTrue(period.isActive());
    }
}
