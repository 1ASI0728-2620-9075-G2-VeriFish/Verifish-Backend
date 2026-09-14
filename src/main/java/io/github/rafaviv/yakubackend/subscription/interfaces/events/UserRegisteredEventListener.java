package io.github.rafaviv.yakubackend.subscription.interfaces.events;

import io.github.rafaviv.yakubackend.iam.domain.model.events.UserRegisteredEvent;
import io.github.rafaviv.yakubackend.subscription.application.internal.commandservices.SubscriptionCommandService;
import io.github.rafaviv.yakubackend.subscription.infrastructure.persistence.jpa.repositories.PlanRepository;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class UserRegisteredEventListener {
    private final SubscriptionCommandService subscriptionCommandService;
    private final PlanRepository planRepository;

    public UserRegisteredEventListener(SubscriptionCommandService subscriptionCommandService,
            PlanRepository planRepository) {
        this.subscriptionCommandService = subscriptionCommandService;
        this.planRepository = planRepository;
    }

    @EventListener
    public void on(UserRegisteredEvent event) {
        planRepository.findByName("FREE").ifPresent(plan -> {
            subscriptionCommandService.subscribeUserToPlan(event.userId(), plan.getId());
        });
    }
}
