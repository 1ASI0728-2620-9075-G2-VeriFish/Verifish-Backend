package io.github.rafaviv.yakubackend.equipment.interfaces.acl;

import io.github.rafaviv.yakubackend.shared.security.FarmOwnershipResolver;

import java.util.Optional;

/**
 * Facade for the Equipment Bounded Context.
 * Exposes internal functionalities to other bounded contexts in a decoupled way.
 */
public interface EquipmentContextFacade extends FarmOwnershipResolver {
    Long getPondIdByDeviceId(String deviceId);

    @Override
    Optional<Long> findFarmIdByOwnerId(Long ownerId);
}
