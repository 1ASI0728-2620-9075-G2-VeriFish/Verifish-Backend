package io.github.rafaviv.yakubackend.shared.security;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class FarmAccessDeniedException extends RuntimeException {

    public FarmAccessDeniedException() {
        super("Access denied to the requested farm resource");
    }

    public FarmAccessDeniedException(String message) {
        super(message);
    }
}
