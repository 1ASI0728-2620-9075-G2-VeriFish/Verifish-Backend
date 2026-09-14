package io.github.rafaviv.yakubackend.subscription.application.internal.exceptions;

public class PlanNotFoundException extends RuntimeException {
    public PlanNotFoundException(Long planId) {
        super("Plan with id " + planId + " not found");
    }
}
