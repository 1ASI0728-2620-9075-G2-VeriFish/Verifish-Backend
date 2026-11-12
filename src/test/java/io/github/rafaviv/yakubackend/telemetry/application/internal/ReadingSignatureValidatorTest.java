package io.github.rafaviv.yakubackend.telemetry.application.internal;

import io.github.rafaviv.yakubackend.telemetry.application.outboundservices.acl.ExternalEquipmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Locale;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReadingSignatureValidatorTest {

    @Mock
    private ExternalEquipmentService externalEquipmentService;

    private ReadingSignatureValidator validator;

    private static final Long FARM_ID = 1L;
    private static final String FARM_TOKEN = "secret-farm-token-12345";
    private static final String RAW_BODY = "{\"deviceId\":\"DEV-01\",\"temperature\":22.5,\"turbidity\":4.0,\"ica\":92.0}";

    @BeforeEach
    void setUp() {
        validator = new ReadingSignatureValidator(externalEquipmentService);
    }

    @Test
    @DisplayName("Validates matching HMAC-SHA256 hex signature")
    void validSignatureReturnsTrue() {
        when(externalEquipmentService.getFarmTokenByFarmId(FARM_ID)).thenReturn(Optional.of(FARM_TOKEN));
        String signature = ReadingSignatureValidator.computeHmacSha256Hex(FARM_TOKEN, RAW_BODY);

        assertTrue(validator.isValid(FARM_ID, RAW_BODY, signature));
        assertTrue(validator.isValid(FARM_ID, RAW_BODY, signature.toUpperCase(Locale.ROOT)));
    }

    @Test
    @DisplayName("Rejects altered body or altered signature")
    void alteredSignatureOrBodyReturnsFalse() {
        when(externalEquipmentService.getFarmTokenByFarmId(FARM_ID)).thenReturn(Optional.of(FARM_TOKEN));
        String signature = ReadingSignatureValidator.computeHmacSha256Hex(FARM_TOKEN, RAW_BODY);

        String alteredBody = "{\"deviceId\":\"DEV-01\",\"temperature\":99.9,\"turbidity\":4.0,\"ica\":92.0}";
        assertFalse(validator.isValid(FARM_ID, alteredBody, signature));

        String alteredSignature = "0".repeat(signature.length());
        assertFalse(validator.isValid(FARM_ID, RAW_BODY, alteredSignature));
    }

    @Test
    @DisplayName("Rejects missing signature, farmId, body, or unknown farm")
    void missingInputsOrUnknownFarmReturnsFalse() {
        assertFalse(validator.isValid(null, RAW_BODY, "abc"));
        assertFalse(validator.isValid(FARM_ID, null, "abc"));
        assertFalse(validator.isValid(FARM_ID, RAW_BODY, null));
        assertFalse(validator.isValid(FARM_ID, RAW_BODY, "   "));

        when(externalEquipmentService.getFarmTokenByFarmId(999L)).thenReturn(Optional.empty());
        assertFalse(validator.isValid(999L, RAW_BODY, "abc"));
    }
}

