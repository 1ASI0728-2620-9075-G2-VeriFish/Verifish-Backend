package io.github.rafaviv.yakubackend.shared.security;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when a telemetry ingest request fails HMAC signature validation
 * or lacks required authentication headers.
 */
@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class InvalidReadingSignatureException extends RuntimeException {

    public InvalidReadingSignatureException(String message) {
        super(message);
    }
}

