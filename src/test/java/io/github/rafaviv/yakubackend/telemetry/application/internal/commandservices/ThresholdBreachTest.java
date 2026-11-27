package io.github.rafaviv.yakubackend.telemetry.application.internal.commandservices;

import io.github.rafaviv.yakubackend.equipment.interfaces.acl.EquipmentContextFacade;
import io.github.rafaviv.yakubackend.telemetry.application.outboundservices.acl.ExternalEquipmentService;
import io.github.rafaviv.yakubackend.telemetry.domain.model.aggregates.Threshold;
import io.github.rafaviv.yakubackend.telemetry.domain.model.commands.ProcessGroupedTelemetryCommand;
import io.github.rafaviv.yakubackend.telemetry.domain.model.events.ThresholdBreachedEvent;
import io.github.rafaviv.yakubackend.telemetry.domain.model.valueobjects.Species;
import io.github.rafaviv.yakubackend.telemetry.infrastructure.persistence.jpa.repositories.SensorPondMappingRepository;
import io.github.rafaviv.yakubackend.telemetry.infrastructure.persistence.jpa.repositories.SensorReadingRepository;
import io.github.rafaviv.yakubackend.telemetry.infrastructure.persistence.jpa.repositories.ThresholdRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ThresholdBreachTest {

    @Mock
    private SensorReadingRepository sensorReadingRepository;
    @Mock
    private ThresholdRepository thresholdRepository;
    @Mock
    private SensorPondMappingRepository sensorPondMappingRepository;
    @Mock
    private ExternalEquipmentService externalEquipmentService;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Mock
    private EquipmentContextFacade equipmentContextFacade;

    private TelemetryCommandServiceImpl telemetryCommandService;

    private static final String DEVICE_ID = "DEV-TRUCHA-01";
    private static final Long POND_ID = 10L;
    private static final Long ADMIN_ID = 1L;
    private static final Long OPERATOR_ID = 2L;

    @BeforeEach
    void setUp() {
        telemetryCommandService = new TelemetryCommandServiceImpl(
                sensorReadingRepository,
                thresholdRepository,
                sensorPondMappingRepository,
                externalEquipmentService,
                eventPublisher,
                equipmentContextFacade
        );

        Threshold truchaThreshold = new Threshold(Species.TRUCHA, 10.0, 18.0, 3.5, 4.5);
        when(equipmentContextFacade.getPondIdByDeviceId(DEVICE_ID)).thenReturn(POND_ID);
        when(externalEquipmentService.getSpeciesByPondId(POND_ID)).thenReturn("TRUCHA");
        when(thresholdRepository.findBySpecies(Species.TRUCHA)).thenReturn(Optional.of(truchaThreshold));
    }

    @Test
    @DisplayName("Reading within thresholds does not publish ThresholdBreachedEvent")
    void withinThresholdsPublishesNoEvent() {
        var command = new ProcessGroupedTelemetryCommand(DEVICE_ID, 14.0, 4.0, 90.0);

        telemetryCommandService.handle(command);

        verify(eventPublisher, never()).publishEvent(any(ThresholdBreachedEvent.class));
    }

    @Test
    @DisplayName("Single metric out of range publishes WARNING ThresholdBreachedEvent to operator")
    void singleAnomalyPublishesWarningEvent() {
        when(externalEquipmentService.getOperatorIdByPondId(POND_ID)).thenReturn(OPERATOR_ID);
        var command = new ProcessGroupedTelemetryCommand(DEVICE_ID, 25.0, 4.0, 70.0);

        telemetryCommandService.handle(command);

        ArgumentCaptor<ThresholdBreachedEvent> captor = ArgumentCaptor.forClass(ThresholdBreachedEvent.class);
        verify(eventPublisher, times(1)).publishEvent(captor.capture());

        ThresholdBreachedEvent event = captor.getValue();
        assertEquals(POND_ID, event.pondId());
        assertEquals(OPERATOR_ID, event.targetUserId());
        assertEquals("WARNING", event.severity());
        assertTrue(event.message().contains("TEMPERATURE"));
    }

    @Test
    @DisplayName("Two metrics out of range publishes CRITICAL ThresholdBreachedEvent to admin and operator")
    void twoAnomaliesPublishCriticalEventsToAdminAndOperator() {
        when(externalEquipmentService.getUserIdByPondId(POND_ID)).thenReturn(ADMIN_ID);
        when(externalEquipmentService.getOperatorIdByPondId(POND_ID)).thenReturn(OPERATOR_ID);
        var command = new ProcessGroupedTelemetryCommand(DEVICE_ID, 25.0, 12.0, 40.0);

        telemetryCommandService.handle(command);

        ArgumentCaptor<ThresholdBreachedEvent> captor = ArgumentCaptor.forClass(ThresholdBreachedEvent.class);
        verify(eventPublisher, times(2)).publishEvent(captor.capture());

        List<ThresholdBreachedEvent> events = captor.getAllValues();
        assertTrue(events.stream().allMatch(e -> "CRITICAL".equals(e.severity())));
        assertTrue(events.stream().anyMatch(e -> ADMIN_ID.equals(e.targetUserId())));
        assertTrue(events.stream().anyMatch(e -> OPERATOR_ID.equals(e.targetUserId())));
    }
}

