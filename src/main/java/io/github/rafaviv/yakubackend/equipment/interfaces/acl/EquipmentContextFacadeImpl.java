package io.github.rafaviv.yakubackend.equipment.interfaces.acl;

import io.github.rafaviv.yakubackend.equipment.domain.model.aggregates.Farm;
import io.github.rafaviv.yakubackend.equipment.infrastructure.persistence.jpa.repositories.EquipmentRepository;
import io.github.rafaviv.yakubackend.equipment.infrastructure.persistence.jpa.repositories.FarmRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.Optional;

@Service
public class EquipmentContextFacadeImpl implements EquipmentContextFacade {

    private final FarmRepository farmRepository;
    private final EquipmentRepository equipmentRepository;

    public EquipmentContextFacadeImpl(FarmRepository farmRepository,
                                      EquipmentRepository equipmentRepository) {
        this.farmRepository = farmRepository;
        this.equipmentRepository = equipmentRepository;
    }

    @Override
    public Long getPondIdByDeviceId(String deviceId) {
        return equipmentRepository.findByPhysicalCode(deviceId)
                .map(io.github.rafaviv.yakubackend.equipment.domain.model.aggregates.Equipment::getPondId)
                .orElseGet(() -> equipmentRepository.findByPhysicalCode(deviceId + "-TEMP")
                        .map(io.github.rafaviv.yakubackend.equipment.domain.model.aggregates.Equipment::getPondId)
                        .orElseThrow(() -> new IllegalArgumentException("Device not found or not linked to any pond: " + deviceId)));
    }

    @Override
    public Optional<Long> findFarmIdByOwnerId(Long ownerId) {
        if (ownerId == null) {
            return Optional.empty();
        }
        return farmRepository.findByOwnerId(ownerId).stream()
                .filter(farm -> farm.getId() != null)
                .min(Comparator.comparing(Farm::getId))
                .map(Farm::getId);
    }
}
