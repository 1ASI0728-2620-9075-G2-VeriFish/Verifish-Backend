package io.github.rafaviv.yakubackend.telemetry.application.internal;

import io.github.rafaviv.yakubackend.telemetry.application.outboundservices.acl.ExternalEquipmentService;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Optional;

/**
 * Validates HMAC-SHA256 hex signatures for raw telemetry payloads using the farm's token as the secret key.
 */
@Service
public class ReadingSignatureValidator {

    private static final String HMAC_SHA256 = "HmacSHA256";
    private static final HexFormat HEX_FORMAT = HexFormat.of();

    private final ExternalEquipmentService externalEquipmentService;

    public ReadingSignatureValidator(ExternalEquipmentService externalEquipmentService) {
        this.externalEquipmentService = externalEquipmentService;
    }

    public boolean isValid(Long farmId, String rawBody, String signature) {
        if (farmId == null || rawBody == null || signature == null || signature.isBlank()) {
            return false;
        }

        Optional<String> farmTokenOpt = externalEquipmentService.getFarmTokenByFarmId(farmId);
        if (farmTokenOpt.isEmpty() || farmTokenOpt.get().isBlank()) {
            return false;
        }

        String expectedHex = computeHmacSha256Hex(farmTokenOpt.get(), rawBody);
        String providedHex = signature.trim().toLowerCase(Locale.ROOT);

        return MessageDigest.isEqual(
                expectedHex.getBytes(StandardCharsets.UTF_8),
                providedHex.getBytes(StandardCharsets.UTF_8)
        );
    }

    public static String computeHmacSha256Hex(String secret, String payload) {
        try {
            Mac mac = Mac.getInstance(HMAC_SHA256);
            SecretKeySpec keySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_SHA256);
            mac.init(keySpec);
            byte[] hmacBytes = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            return HEX_FORMAT.formatHex(hmacBytes);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException("Failed to compute HMAC-SHA256 signature", e);
        }
    }
}

