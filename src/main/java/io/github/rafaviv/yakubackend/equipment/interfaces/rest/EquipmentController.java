package io.github.rafaviv.yakubackend.equipment.interfaces.rest;

import io.github.rafaviv.yakubackend.equipment.domain.model.aggregates.Equipment;
import io.github.rafaviv.yakubackend.equipment.domain.model.queries.GetEquipmentByFarmIdQuery;
import io.github.rafaviv.yakubackend.equipment.domain.model.queries.GetEquipmentByIdQuery;
import io.github.rafaviv.yakubackend.equipment.domain.model.queries.GetPondByIdQuery;
import io.github.rafaviv.yakubackend.equipment.domain.model.valueobjects.EquipmentType;
import io.github.rafaviv.yakubackend.equipment.domain.services.EquipmentCommandService;
import io.github.rafaviv.yakubackend.equipment.domain.services.EquipmentQueryService;
import io.github.rafaviv.yakubackend.equipment.domain.services.PondQueryService;
import io.github.rafaviv.yakubackend.equipment.interfaces.rest.transform.RegisterEquipmentResource;
import io.github.rafaviv.yakubackend.equipment.interfaces.rest.transform.RegisterIoTDeviceResource;
import io.github.rafaviv.yakubackend.shared.security.FarmAccessGuard;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/equipment")
public class EquipmentController {

    private final EquipmentCommandService equipmentCommandService;
    private final EquipmentQueryService equipmentQueryService;
    private final PondQueryService pondQueryService;
    private final FarmAccessGuard farmAccessGuard;

    public EquipmentController(EquipmentCommandService equipmentCommandService,
                               EquipmentQueryService equipmentQueryService,
                               PondQueryService pondQueryService,
                               FarmAccessGuard farmAccessGuard) {
        this.equipmentCommandService = equipmentCommandService;
        this.equipmentQueryService = equipmentQueryService;
        this.pondQueryService = pondQueryService;
        this.farmAccessGuard = farmAccessGuard;
    }

    @PostMapping
    public ResponseEntity<?> registerEquipment(@RequestBody RegisterEquipmentResource resource) {
        farmAccessGuard.requireSameFarm(resource.farmId());
        try {
            var equipment = equipmentCommandService.registerEquipment(
                    EquipmentType.valueOf(resource.type().toUpperCase()), resource.name(), resource.physicalCode(),
                    resource.farmId());
            if (equipment.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            return ResponseEntity.status(HttpStatus.CREATED).body(equipment.get());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(e.getMessage());
        }
    }

    @PostMapping("/iot-device")
    public ResponseEntity<?> registerIoTDevice(@RequestBody RegisterIoTDeviceResource resource) {
        farmAccessGuard.requireSameFarm(resource.farmId());
        try {
            var equipments = equipmentCommandService.registerIoTDevice(resource.deviceId(), resource.deviceName(),
                    resource.farmId());
            if (equipments.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            return ResponseEntity.status(HttpStatus.CREATED).body(equipments);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/{equipmentId}/link/{pondId}")
    public ResponseEntity<?> linkEquipmentToPond(@PathVariable Long equipmentId, @PathVariable Long pondId) {
        var equipmentOpt = equipmentQueryService.handle(new GetEquipmentByIdQuery(equipmentId));
        if (equipmentOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        var pondOpt = pondQueryService.handle(new GetPondByIdQuery(pondId));
        if (pondOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        farmAccessGuard.requireSameFarm(equipmentOpt.get().getFarmId());
        farmAccessGuard.requireSameFarm(pondOpt.get().getFarmId());

        var equipment = equipmentCommandService.linkEquipmentToPond(equipmentId, pondId);
        if (equipment.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(equipment.get());
    }

    @GetMapping
    public ResponseEntity<List<Equipment>> getAllEquipment(
            @RequestParam(required = false) Long farmId) {
        Long currentFarmId = farmAccessGuard.requireCurrentFarmId();
        if (farmId != null) {
            farmAccessGuard.requireSameFarm(farmId);
        }
        return ResponseEntity.ok(equipmentQueryService.handle(new GetEquipmentByFarmIdQuery(currentFarmId)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getEquipmentById(@PathVariable Long id) {
        Optional<Equipment> equipment = equipmentQueryService.handle(new GetEquipmentByIdQuery(id));
        if (equipment.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Equipment not found with id: " + id);
        }
        farmAccessGuard.requireSameFarm(equipment.get().getFarmId());
        return ResponseEntity.ok(equipment.get());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEquipment(@PathVariable Long id) {
        Optional<Equipment> equipment = equipmentQueryService.handle(new GetEquipmentByIdQuery(id));
        if (equipment.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        farmAccessGuard.requireSameFarm(equipment.get().getFarmId());
        try {
            equipmentCommandService.deleteEquipment(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{equipmentId}/command")
    public ResponseEntity<?> executeRemoteCommand(@PathVariable Long equipmentId) {
        Optional<Equipment> equipment = equipmentQueryService.handle(new GetEquipmentByIdQuery(equipmentId));
        if (equipment.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        farmAccessGuard.requireSameFarm(equipment.get().getFarmId());
        try {
            equipmentCommandService.executeRemoteCommand(equipmentId);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error executing command");
        }
    }
}
