package io.github.rafaviv.yakubackend.shared.security;

/**
 * Contract for security principals that carry a tenant farm identifier.
 */
public interface FarmAwarePrincipal {

    Long getFarmId();
}

