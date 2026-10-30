package io.github.rafaviv.yakubackend.telemetry.interfaces.rest;

import io.github.rafaviv.yakubackend.shared.security.FarmAccessGuard;
import io.github.rafaviv.yakubackend.telemetry.application.internal.commandservices.TelemetryCommandService;
import io.github.rafaviv.yakubackend.telemetry.application.internal.queryservices.TelemetryQueryService;
import io.github.rafaviv.yakubackend.telemetry.application.outboundservices.acl.ExternalEquipmentService;
import io.github.rafaviv.yakubackend.telemetry.domain.model.aggregates.MeasurementAggregate;
import io.github.rafaviv.yakubackend.telemetry.domain.model.aggregates.SensorReading;
import io.github.rafaviv.yakubackend.telemetry.domain.model.queries.GetHistoricalDataQuery;
import io.github.rafaviv.yakubackend.telemetry.domain.model.queries.GetPondStatusQuery;
import io.github.rafaviv.yakubackend.telemetry.domain.model.valueobjects.TimeFilter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/telemetry")
public class TelemetryController {

    private final TelemetryQueryService telemetryQueryService;
    private final TelemetryCommandService telemetryCommandService;
    private final ExternalEquipmentService externalEquipmentService;
    private final FarmAccessGuard farmAccessGuard;

    public TelemetryController(TelemetryQueryService telemetryQueryService,
                               TelemetryCommandService telemetryCommandService,
                               ExternalEquipmentService externalEquipmentService,
                               FarmAccessGuard farmAccessGuard) {
        this.telemetryQueryService = telemetryQueryService;
        this.telemetryCommandService = telemetryCommandService;
        this.externalEquipmentService = externalEquipmentService;
        this.farmAccessGuard = farmAccessGuard;
    }

    @GetMapping("/ponds/{pondId}/status")
    public ResponseEntity<List<SensorReading>> getPondStatus(@PathVariable Long pondId) {
        Optional<Long> pondFarmId = externalEquipmentService.getFarmIdByPondId(pondId);
        if (pondFarmId.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        farmAccessGuard.requireSameFarm(pondFarmId.get());

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
        Optional<Long> pondFarmId = externalEquipmentService.getFarmIdByPondId(pondId);
        if (pondFarmId.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        farmAccessGuard.requireSameFarm(pondFarmId.get());

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
