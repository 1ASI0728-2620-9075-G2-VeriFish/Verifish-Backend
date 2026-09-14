    package io.github.rafaviv.yakubackend.subscription.domain.model.aggregates;

    import io.github.rafaviv.yakubackend.subscription.domain.model.entities.Plan;
    import io.github.rafaviv.yakubackend.subscription.domain.model.valueobjects.Currency;
    import io.github.rafaviv.yakubackend.subscription.domain.model.valueobjects.SubscriptionPeriod;
    import io.github.rafaviv.yakubackend.subscription.domain.model.valueobjects.SubscriptionStatus;
    import org.junit.jupiter.api.DisplayName;
    import org.junit.jupiter.api.Test;

    import java.time.LocalDate;

    import static org.junit.jupiter.api.Assertions.*;

    class SubscriptionTest {

        @Test
        @DisplayName("Given a new user, When assigning a plan, Then subscription becomes active with correct period")
        void subscribeToPlan_Successfully() {
            // Arrange
            Long userId = 1L;
            Plan premiumPlan = new Plan("PREMIUM", 19.99, Currency.USD, 10, 30, null); // 30 days duration
            Subscription subscription = new Subscription(userId); // Initial status is CANCELLED

            // Act
            subscription.subscribeToPlan(premiumPlan);

            // Assert
            assertEquals(SubscriptionStatus.ACTIVE, subscription.getStatus(), "Status should be ACTIVE");
            assertEquals(premiumPlan, subscription.getPlan(), "Plan should be PREMIUM");
            assertNotNull(subscription.getPeriod(), "Period must be initialized");

            // Assert Business Rule: End date should be today + 30 days
            LocalDate expectedEndDate = LocalDate.now().plusDays(30);
            assertEquals(expectedEndDate, subscription.getPeriod().endDate(), "End date calculation is incorrect");
        }

        @Test
        @DisplayName("Given an active subscription, When cancelled, Then status becomes CANCELLED")
        void cancelSubscription_Successfully() {
            // Arrange
            Subscription subscription = new Subscription(1L);
            subscription.subscribeToPlan(new Plan("FREE", 0.0, Currency.USD, 1, 365, null));

            // Act
            subscription.cancel();

            // Assert
            assertEquals(SubscriptionStatus.CANCELLED, subscription.getStatus());
        }

        @Test
        @DisplayName("Given a subscription, When expired, Then status becomes EXPIRED")
        void expireSubscription_Successfully() {
            Subscription subscription = new Subscription(1L);
            subscription.subscribeToPlan(new Plan("BASIC", 9.99, Currency.USD, 5, 30, null));

            subscription.expire();

            assertEquals(SubscriptionStatus.EXPIRED, subscription.getStatus());
        }

        @Test
        @DisplayName("Given user ID, When creating subscription with userId only, Then status is CANCELLED")
        void createSubscription_UserIdOnly_StatusCancelled() {
            Subscription subscription = new Subscription(42L);

            assertEquals(42L, subscription.getUserId());
            assertEquals(SubscriptionStatus.CANCELLED, subscription.getStatus());
            assertNull(subscription.getPlan());
            assertNull(subscription.getPeriod());
        }

        @Test
        @DisplayName("Given full data, When creating subscription with full constructor, Then all fields are set")
        void createSubscription_FullConstructor_AllFieldsSet() {
            Plan plan = new Plan("ENTERPRISE", 99.99, Currency.USD, 100, 365, null);
            SubscriptionPeriod period = new SubscriptionPeriod(LocalDate.now(), LocalDate.now().plusDays(365));

            Subscription subscription = new Subscription(1L, plan, period);

            assertEquals(1L, subscription.getUserId());
            assertEquals(plan, subscription.getPlan());
            assertEquals(period, subscription.getPeriod());
            assertEquals(SubscriptionStatus.ACTIVE, subscription.getStatus());
        }

        @Test
        @DisplayName("Given cancelled subscription, When subscribing to plan, Then becomes active")
        void subscribeToPlan_FromCancelled_StatusBecomesActive() {
            Subscription subscription = new Subscription(1L);
            assertEquals(SubscriptionStatus.CANCELLED, subscription.getStatus());

            Plan plan = new Plan("STARTER", 4.99, Currency.USD, 3, 14, null);
            subscription.subscribeToPlan(plan);

            assertEquals(SubscriptionStatus.ACTIVE, subscription.getStatus());
        }

        @Test
        @DisplayName("Given subscription, When subscribing to different plan, Then plan is updated")
        void subscribeToPlan_ChangePlan_PlanUpdated() {
            Subscription subscription = new Subscription(1L);
            Plan basicPlan = new Plan("BASIC", 9.99, Currency.USD, 5, 30, null);
            subscription.subscribeToPlan(basicPlan);

            Plan premiumPlan = new Plan("PREMIUM", 19.99, Currency.USD, 10, 30, null);
            subscription.subscribeToPlan(premiumPlan);

            assertEquals(premiumPlan, subscription.getPlan());
            assertEquals(SubscriptionStatus.ACTIVE, subscription.getStatus());
        }
    }