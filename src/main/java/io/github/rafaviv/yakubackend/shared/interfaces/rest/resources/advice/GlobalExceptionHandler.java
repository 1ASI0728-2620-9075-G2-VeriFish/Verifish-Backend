package io.github.rafaviv.yakubackend.shared.interfaces.rest.resources.advice;

import io.github.rafaviv.yakubackend.shared.interfaces.rest.resources.ApiErrorResource;
import io.github.rafaviv.yakubackend.shared.security.FarmAccessDeniedException;
import io.github.rafaviv.yakubackend.shared.security.FarmAuthenticationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(FarmAccessDeniedException.class)
    public ResponseEntity<ApiErrorResource> handleFarmAccessDeniedException(
            FarmAccessDeniedException ex, WebRequest request) {

        ApiErrorResource error = new ApiErrorResource(
                "Access denied to the requested farm resource",
                HttpStatus.FORBIDDEN.value(),
                "Forbidden",
                request.getDescription(false).replace("uri=", "")
        );
        return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(FarmAuthenticationException.class)
    public ResponseEntity<ApiErrorResource> handleFarmAuthenticationException(
            FarmAuthenticationException ex, WebRequest request) {

        ApiErrorResource error = new ApiErrorResource(
                "Authentication is required to access this resource",
                HttpStatus.UNAUTHORIZED.value(),
                "Unauthorized",
                request.getDescription(false).replace("uri=", "")
        );
        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResource> handleIllegalArgumentException(
            IllegalArgumentException ex, WebRequest request) {

        ApiErrorResource error = new ApiErrorResource(
                ex.getMessage(),
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                request.getDescription(false).replace("uri=", "")
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiErrorResource> handleIllegalStateException(
            IllegalStateException ex, WebRequest request) {

        ApiErrorResource error = new ApiErrorResource(
                ex.getMessage(),
                HttpStatus.CONFLICT.value(),
                "Conflict",
                request.getDescription(false).replace("uri=", "")
        );
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }
}
