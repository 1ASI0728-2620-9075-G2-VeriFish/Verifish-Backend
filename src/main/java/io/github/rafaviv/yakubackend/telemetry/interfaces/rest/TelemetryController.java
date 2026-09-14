package io.github.rafaviv.yakubackend.telemetry.interfaces.rest;

import io.github.rafaviv.yakubackend.telemetry.application.internal.queryservices.TelemetryQueryService;
import io.github.rafaviv.yakubackend.telemetry.domain.model.aggregates.MeasurementAggregate;
import io.github.rafaviv.yakubackend.telemetry.domain.model.aggregates.SensorReading;
import io.github.rafaviv.yakubackend.telemetry.domain.model.queries.GetHistoricalDataQuery;
import io.github.rafaviv.yakubackend.telemetry.domain.model.queries.GetPondStatusQuery;
import io.github.rafaviv.yakubackend.telemetry.domain.model.valueobjects.TimeFilter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/telemetry")
public class TelemetryController {

    private final TelemetryQueryService telemetryQueryService;
    private final io.github.rafaviv.yakubackend.telemetry.application.internal.commandservices.TelemetryCommandService telemetryCommandService;

    public TelemetryController(TelemetryQueryService telemetryQueryService, io.github.rafaviv.yakubackend.telemetry.application.internal.commandservices.TelemetryCommandService telemetryCommandService) {
        this.telemetryQueryService = telemetryQueryService;
        this.telemetryCommandService = telemetryCommandService;
    }

    @GetMapping("/ponds/{pondId}/status")
    public ResponseEntity<List<SensorReading>> getPondStatus(@PathVariable Long pondId) {
        List<SensorReading> readings = telemetryQueryService.handle(new GetPondStatusQuery(pondId));
        if (readings == null || readings.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(readings);
    }

    @GetMapping("/ponds/{pondId}/historical")
    public ResponseEntity<List<MeasurementAggregate>> getHistoricalData(
            @PathVariable Long pondId,
            @RequestParam(defaultValue = "WEEKLY") String timeFilter) {
        try {
            TimeFilter filter = TimeFilter.valueOf(timeFilter.toUpperCase());
            List<MeasurementAggregate> aggregates = telemetryQueryService.handle(new GetHistoricalDataQuery(pondId, filter));
            return ResponseEntity.ok(aggregates);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/manual-ingest")
    public ResponseEntity<String> manualIngest(@RequestBody io.github.rafaviv.yakubackend.telemetry.domain.model.commands.ProcessGroupedTelemetryCommand command) {
        try {
            telemetryCommandService.handle(command);
            return ResponseEntity.ok("Telemetry ingested successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
