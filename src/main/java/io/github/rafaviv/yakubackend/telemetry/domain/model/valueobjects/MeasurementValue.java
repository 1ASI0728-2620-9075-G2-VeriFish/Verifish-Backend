package io.github.rafaviv.yakubackend.telemetry.domain.model.valueobjects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;

@Embeddable
@Getter
public class MeasurementValue {

    @Column(name = "\"value\"")
    private Double value;
    private String unit;

    protected MeasurementValue() {
    }

    public MeasurementValue(Double value, String unit) {
        if (value == null) {
            throw new IllegalArgumentException("Measurement value cannot be null");
        }
        this.value = value;
        this.unit = unit != null ? unit : "";
    }
}
