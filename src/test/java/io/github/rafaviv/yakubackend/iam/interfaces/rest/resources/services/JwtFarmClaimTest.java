package io.github.rafaviv.yakubackend.iam.interfaces.rest.resources.services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class JwtFarmClaimTest {

    private static final String TEST_SECRET = "TestSecretKeyForJwtVerificationMustBeAtLeast256BitsLong123456";
    private TokenServiceImpl tokenService;
    private SecretKey signingKey;

    @BeforeEach
    void setUp() {
        tokenService = new TokenServiceImpl();
        ReflectionTestUtils.setField(tokenService, "secret", TEST_SECRET);
        ReflectionTestUtils.setField(tokenService, "expirationDays", 7);
        signingKey = Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    @DisplayName("Token generated with farmId contains farmId claim, getFarmIdFromToken returns it, and workshop_id does not exist")
    void generateToken_WithFarmId_ContainsFarmIdClaimAndNoWorkshopId() {
        Long userId = 15L;
        Long farmId = 99L;
        String token = tokenService.generateToken(userId, "ADMIN", farmId);

        assertNotNull(token);
        assertTrue(tokenService.validateToken(token));
        assertEquals(userId, tokenService.getUserIdFromToken(token));
        assertEquals(farmId, tokenService.getFarmIdFromToken(token));

        Claims claims = Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        assertEquals(farmId, claims.get("farmId", Number.class).longValue());
        assertFalse(claims.containsKey("workshop_id"));
        assertNull(claims.get("workshop_id"));
    }

    @Test
    @DisplayName("Token generated without farmId returns null from getFarmIdFromToken and omits farmId and workshop_id claims")
    void generateToken_WithoutFarmId_ReturnsNull() {
        Long userId = 20L;
        String tokenTwoArgs = tokenService.generateToken(userId, "OPERATOR");
        String tokenNullFarm = tokenService.generateToken(userId, "OPERATOR", null);

        assertNull(tokenService.getFarmIdFromToken(tokenTwoArgs));
        assertNull(tokenService.getFarmIdFromToken(tokenNullFarm));

        Claims claims = Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(tokenTwoArgs)
                .getPayload();

        assertFalse(claims.containsKey("farmId"));
        assertFalse(claims.containsKey("workshop_id"));
    }
}

