package io.github.rafaviv.yakubackend.iam.application.internal.outboundservices.tokens;

/**
 * TokenService interface
 * This interface is used to generate and validate tokens
 */
public interface TokenService {

    /**
     * Generate a token for a given user ID and role without a farm ID
     * @param userId the user ID
     * @param userRole the user role
     * @return String the token
     */
    default String generateToken(Long userId, String userRole) {
        return generateToken(userId, userRole, null);
    }

    /**
     * Generate a token for a given user ID, role, and farm ID
     * @param userId the user ID
     * @param userRole the user role
     * @param farmId the farm ID (nullable)
     * @return String the token
     */
    String generateToken(Long userId, String userRole, Long farmId);

    /**
     * Extract the user ID from a token
     * @param token the token
     * @return Long the userId
     */
    Long getUserIdFromToken(String token);

    /**
     * Extract the farm ID from a token
     * @param token the token
     * @return Long the farm ID, or null if not present
     */
    Long getFarmIdFromToken(String token);

    /**
     * Validate a token
     * @param token the token
     * @return boolean true if the token is valid, false otherwise
     */
    boolean validateToken(String token);
}
