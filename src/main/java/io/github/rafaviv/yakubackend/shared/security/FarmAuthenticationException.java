package io.github.rafaviv.yakubackend.shared.security;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class FarmAuthenticationException extends RuntimeException {

    public FarmAuthenticationException() {
        super("Authentication is required to access this resource");
    }

    public FarmAuthenticationException(String message) {
        super(message);
    }
}
