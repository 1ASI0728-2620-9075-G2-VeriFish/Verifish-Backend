package io.github.rafaviv.yakubackend.shared.security;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class FarmAccessGuard {

    /**
     * Reads the farmId of the currently authenticated user from the SecurityContext.
     *
     * @return the farmId from the authenticated principal, or null if not available
     */
    public Long currentFarmId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!isAuthenticated(authentication)) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof FarmAwarePrincipal farmAwarePrincipal) {
            return farmAwarePrincipal.getFarmId();
        }
        return null;
    }

    /**
     * Ensures the request is authenticated and returns the authenticated user's farmId.
     * Throws 401 if unauthenticated, or 403 if the user has no farmId in their token.
     *
     * @return the non-null farmId of the authenticated user
     */
    public Long requireCurrentFarmId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!isAuthenticated(authentication)) {
            throw new FarmAuthenticationException();
        }
        Long userFarmId = currentFarmId();
        if (userFarmId == null) {
            throw new FarmAccessDeniedException();
        }
        return userFarmId;
    }

    /**
     * Verifies that the authenticated user belongs to the same farm as the target resource.
     * Throws 401 if unauthenticated, or 403 (FarmAccessDeniedException) if the token's farmId
     * is null or does not match resourceFarmId.
     *
     * @param resourceFarmId the farmId of the target resource
     */
    public void requireSameFarm(Long resourceFarmId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!isAuthenticated(authentication)) {
            throw new FarmAuthenticationException();
        }
        Long userFarmId = currentFarmId();
        if (userFarmId == null || resourceFarmId == null || !userFarmId.equals(resourceFarmId)) {
            throw new FarmAccessDeniedException();
        }
    }

    private boolean isAuthenticated(Authentication authentication) {
        return authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)
                && !"anonymousUser".equals(authentication.getPrincipal());
    }
}
