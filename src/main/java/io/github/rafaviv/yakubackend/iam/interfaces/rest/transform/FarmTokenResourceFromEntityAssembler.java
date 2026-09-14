package io.github.rafaviv.yakubackend.iam.interfaces.rest.transform;

import io.github.rafaviv.yakubackend.iam.domain.model.aggregates.FarmToken;
import io.github.rafaviv.yakubackend.iam.interfaces.rest.resources.FarmTokenResource;

public class FarmTokenResourceFromEntityAssembler {
    public static FarmTokenResource toResourceFromEntity(FarmToken entity) {
        return new FarmTokenResource(entity.getId(), entity.getToken(), entity.getFarmId(), entity.isUsed());
    }
}
