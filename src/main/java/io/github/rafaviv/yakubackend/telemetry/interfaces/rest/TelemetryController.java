package io.github.rafaviv.yakubackend.telemetry.interfaces.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.rafaviv.yakubackend.shared.security.FarmAccessGuard;
import io.github.rafaviv.yakubackend.shared.security.InvalidReadingSignatureException;
import io.github.rafaviv.yakubackend.telemetry.application.internal.ReadingSignatureValidator;
import io.github.rafaviv.yakubackend.telemetry.application.internal.commandservices.TelemetryCommandService;
import io.github.rafaviv.yakubackend.telemetry.application.internal.queryservices.TelemetryQueryService;
import io.github.rafaviv.yakubackend.telemetry.application.outboundservices.acl.ExternalEquipmentService;
import io.github.rafaviv.yakubackend.telemetry.domain.model.aggregates.MeasurementAggregate;
import io.github.rafaviv.yakubackend.telemetry.domain.model.aggregates.SensorReading;
import io.github.rafaviv.yakubackend.telemetry.domain.model.commands.ProcessGroupedTelemetryCommand;
import io.github.rafaviv.yakubackend.telemetry.domain.model.queries.GetHistoricalDataQuery;
import io.github.rafaviv.yakubackend.telemetry.domain.model.queries.GetPondStatusQuery;
import io.github.rafaviv.yakubackend.telemetry.domain.model.valueobjects.TimeFilter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/telemetry")
@Tag(name = "Telemetry", description = "Telemetry ingestion and pond monitoring endpoints")
public class TelemetryController {

    private static final Logger LOGGER = LoggerFactory.getLogger(TelemetryController.class);

    private final TelemetryQueryService telemetryQueryService;
    private final TelemetryCommandService telemetryCommandService;
    private final ExternalEquipmentService externalEquipmentService;
    private final FarmAccessGuard farmAccessGuard;
    private final ReadingSignatureValidator readingSignatureValidator;
    private final ObjectMapper objectMapper;

    public TelemetryController(TelemetryQueryService telemetryQueryService,
                               TelemetryCommandService telemetryCommandService,
                               ExternalEquipmentService externalEquipmentService,
                               FarmAccessGuard farmAccessGuard,
                               ReadingSignatureValidator readingSignatureValidator,
                               ObjectMapper objectMapper) {
        this.telemetryQueryService = telemetryQueryService;
        this.telemetryCommandService = telemetryCommandService;
        this.externalEquipmentService = externalEquipmentService;
        this.farmAccessGuard = farmAccessGuard;
        this.readingSignatureValidator = readingSignatureValidator;
        this.objectMapper = objectMapper;
    }

    @GetMapping("/ponds/{pondId}/status")
    @Operation(summary = "Get current telemetry status for a pond")
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
    @Operation(summary = "Get historical aggregated telemetry data for a pond")
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
    @Operation(summary = "Ingest grouped telemetry readings signed with farm HMAC-SHA256")
    public ResponseEntity<String> manualIngest(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Grouped telemetry payload",
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ProcessGroupedTelemetryCommand.class)
                    )
            )
            @RequestBody(required = false) String rawBody,
            @RequestHeader(value = "X-Farm-Id", required = false) String farmIdHeader,
            @RequestHeader(value = "X-Signature", required = false) String signature) {
        Long farmId = parseFarmId(farmIdHeader);
        if (farmId == null || signature == null || signature.isBlank()
                || rawBody == null || !readingSignatureValidator.isValid(farmId, rawBody, signature)) {
            LOGGER.warn("Rejected telemetry ingest due to missing or invalid HMAC signature for farmId={}", farmId);
            throw new InvalidReadingSignatureException("Invalid or missing HMAC signature");
        }

        try {
            ProcessGroupedTelemetryCommand command = objectMapper.readValue(rawBody, ProcessGroupedTelemetryCommand.class);
            telemetryCommandService.handle(command);
            return ResponseEntity.ok("Telemetry ingested successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    private Long parseFarmId(String farmIdHeader) {
        if (farmIdHeader == null || farmIdHeader.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(farmIdHeader.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
