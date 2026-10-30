package io.github.rafaviv.yakubackend.shared.security;

import java.util.Optional;

/**
 * Outbound port to resolve the farm owned by an administrator user ID
 * without coupling IAM directly to the Equipment bounded context.
 */
public interface FarmOwnershipResolver {

    Optional<Long> findFarmIdByOwnerId(Long ownerId);
}

