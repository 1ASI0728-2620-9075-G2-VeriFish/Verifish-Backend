package io.github.rafaviv.yakubackend.subscription.interfaces.rest.transform;

import io.github.rafaviv.yakubackend.subscription.domain.model.entities.Plan;
import io.github.rafaviv.yakubackend.subscription.interfaces.rest.resources.PlanResource;

public class PlanResourceFromEntityAssembler {
    public static PlanResource toResourceFromEntity(Plan entity) {
        return new PlanResource(
                entity.getId(),
                entity.getName(),
                entity.getPrice(),
                entity.getCurrency().name(),
                entity.getMaxPonds(),
                entity.getDurationInDays(),
                entity.getStripePriceId());
    }
}
