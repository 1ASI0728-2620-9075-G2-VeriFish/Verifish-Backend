package io.github.rafaviv.yakubackend.equipment.interfaces.rest;

import io.github.rafaviv.yakubackend.equipment.domain.model.queries.GetPondByIdQuery;
import io.github.rafaviv.yakubackend.equipment.domain.model.queries.GetPondsByAssignedOperatorIdQuery;
import io.github.rafaviv.yakubackend.equipment.domain.model.queries.GetPondsByFarmIdQuery;
import io.github.rafaviv.yakubackend.equipment.domain.model.valueobjects.Species;
import io.github.rafaviv.yakubackend.equipment.domain.services.PondCommandService;
import io.github.rafaviv.yakubackend.equipment.domain.services.PondQueryService;
import io.github.rafaviv.yakubackend.equipment.interfaces.rest.resources.PondResource;
import io.github.rafaviv.yakubackend.equipment.interfaces.rest.transform.CreatePondResource;
import io.github.rafaviv.yakubackend.equipment.interfaces.rest.transform.PondResourceFromEntityAssembler;
import io.github.rafaviv.yakubackend.shared.security.FarmAccessGuard;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/api/v1/ponds", produces = MediaType.APPLICATION_JSON_VALUE)
public class PondController {

    private final PondCommandService pondCommandService;
    private final PondQueryService pondQueryService;
    private final FarmAccessGuard farmAccessGuard;

    public PondController(PondCommandService pondCommandService,
                          PondQueryService pondQueryService,
                          FarmAccessGuard farmAccessGuard) {
        this.pondCommandService = pondCommandService;
        this.pondQueryService = pondQueryService;
        this.farmAccessGuard = farmAccessGuard;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PondResource> createPond(@RequestBody CreatePondResource resource) {
        farmAccessGuard.requireSameFarm(resource.farmId());
        var pond = pondCommandService.createPond(resource.farmId(), resource.name(), Species.valueOf(resource.species()), resource.volume());
        if (pond.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        var pondResource = PondResourceFromEntityAssembler.toResourceFromEntity(pond.get());
        return ResponseEntity.status(HttpStatus.CREATED).body(pondResource);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('OPERATOR')")
    public ResponseEntity<List<PondResource>> getAllPonds() {
        Long farmId = farmAccessGuard.requireCurrentFarmId();
        var query = new GetPondsByFarmIdQuery(farmId);
        var ponds = pondQueryService.handle(query);
        var resources = ponds.stream()
                .map(PondResourceFromEntityAssembler::toResourceFromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(resources);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OPERATOR')")
    public ResponseEntity<PondResource> getPondById(@PathVariable Long id) {
        var query = new GetPondByIdQuery(id);
        var pond = pondQueryService.handle(query);
        if (pond.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        farmAccessGuard.requireSameFarm(pond.get().getFarmId());
        var pondResource = PondResourceFromEntityAssembler.toResourceFromEntity(pond.get());
        return ResponseEntity.ok(pondResource);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletePond(@PathVariable Long id) {
        var pond = pondQueryService.handle(new GetPondByIdQuery(id));
        if (pond.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        farmAccessGuard.requireSameFarm(pond.get().getFarmId());
        try {
            pondCommandService.deletePond(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/operator/{operatorId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OPERATOR')")
    public ResponseEntity<List<PondResource>> getPondsByOperatorId(@PathVariable Long operatorId) {
        Long farmId = farmAccessGuard.requireCurrentFarmId();
        var query = new GetPondsByAssignedOperatorIdQuery(operatorId);
        var ponds = pondQueryService.handle(query);
        var resources = ponds.stream()
                .filter(pond -> farmId.equals(pond.getFarmId()))
                .map(PondResourceFromEntityAssembler::toResourceFromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(resources);
    }

    @GetMapping("/farm/{farmId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OPERATOR')")
    public ResponseEntity<List<PondResource>> getPondsByFarmId(@PathVariable Long farmId) {
        farmAccessGuard.requireSameFarm(farmId);
        var query = new GetPondsByFarmIdQuery(farmId);
        var ponds = pondQueryService.handle(query);
        var resources = ponds.stream()
                .map(PondResourceFromEntityAssembler::toResourceFromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(resources);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PondResource> updatePond(@PathVariable Long id, @RequestBody CreatePondResource resource) {
        var existingPond = pondQueryService.handle(new GetPondByIdQuery(id));
        if (existingPond.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        farmAccessGuard.requireSameFarm(existingPond.get().getFarmId());
        return pondCommandService.updatePond(id, resource.name(), Species.valueOf(resource.species()), resource.volume())
                .map(pond -> ResponseEntity.ok(PondResourceFromEntityAssembler.toResourceFromEntity(pond)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{pondId}/assignments")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PondResource> assignOperator(@PathVariable Long pondId, @RequestBody Map<String, Long> body) {
        var existingPond = pondQueryService.handle(new GetPondByIdQuery(pondId));
        if (existingPond.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        farmAccessGuard.requireSameFarm(existingPond.get().getFarmId());
        Long operatorId = body.get("operatorId");
        if (operatorId == null) return ResponseEntity.badRequest().build();
        return pondCommandService.assignOperator(pondId, operatorId)
                .map(pond -> ResponseEntity.ok(PondResourceFromEntityAssembler.toResourceFromEntity(pond)))
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{pondId}/deassignments/{operatorId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PondResource> deassignOperator(@PathVariable Long pondId, @PathVariable Long operatorId) {
        var existingPond = pondQueryService.handle(new GetPondByIdQuery(pondId));
        if (existingPond.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        farmAccessGuard.requireSameFarm(existingPond.get().getFarmId());
        return pondCommandService.deassignOperator(pondId)
                .map(pond -> ResponseEntity.ok(PondResourceFromEntityAssembler.toResourceFromEntity(pond)))
                .orElse(ResponseEntity.notFound().build());
    }
}
