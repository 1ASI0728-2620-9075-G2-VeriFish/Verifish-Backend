package io.github.rafaviv.yakubackend.steps;

import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Dado;
import io.cucumber.java.es.Entonces;
import io.github.rafaviv.yakubackend.equipment.domain.model.aggregates.Equipment;
import io.github.rafaviv.yakubackend.equipment.domain.model.valueobjects.EquipmentType;
import io.github.rafaviv.yakubackend.equipment.domain.model.valueobjects.Species;
import io.github.rafaviv.yakubackend.equipment.infrastructure.persistence.jpa.repositories.EquipmentRepository;
import io.github.rafaviv.yakubackend.telemetry.application.internal.ReadingSignatureValidator;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

public class ReadingSignatureSteps {

    private final MockMvc mockMvc;
    private final BddScenarioContext context;
    private final EquipmentRepository equipmentRepository;

    private String deviceId;

    public ReadingSignatureSteps(MockMvc mockMvc,
                                 BddScenarioContext context,
                                 EquipmentRepository equipmentRepository) {
        this.mockMvc = mockMvc;
        this.context = context;
        this.equipmentRepository = equipmentRepository;
    }

    @Dado("que existe una granja registrada con un estanque y un dispositivo IoT {string} vinculado")
    public void granjaConEstanqueYDispositivoVinculado(String deviceId) {
        this.deviceId = deviceId;
        context.initAuthenticatedAdminWithFarmAndPond(Species.TRUCHA);

        Equipment sensor = new Equipment(EquipmentType.SENSOR, "Sensor Temp", deviceId + "-TEMP", context.getFarm().getId());
        sensor.linkToPond(context.getPond().getId());
        equipmentRepository.save(sensor);
    }

    @Cuando("el dispositivo envia una lectura de telemetria firmada correctamente con HMAC-SHA256")
    public void enviaLecturaFirmadaCorrectamente() throws Exception {
        String rawBody = String.format("{\"deviceId\":\"%s\",\"temperature\":14.5,\"turbidity\":4.0,\"ica\":92.0}", deviceId);
        String signature = ReadingSignatureValidator.computeHmacSha256Hex(context.getFarm().getFarmToken(), rawBody);

        context.setLastResult(mockMvc.perform(post("/api/v1/telemetry/manual-ingest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Farm-Id", context.getFarm().getId())
                        .header("X-Signature", signature)
                        .content(rawBody))
                .andReturn());
    }

    @Cuando("el dispositivo envia una lectura de telemetria con una firma HMAC alterada")
    public void enviaLecturaConFirmaAlterada() throws Exception {
        String rawBody = String.format("{\"deviceId\":\"%s\",\"temperature\":14.5,\"turbidity\":4.0,\"ica\":92.0}", deviceId);

        context.setLastResult(mockMvc.perform(post("/api/v1/telemetry/manual-ingest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Farm-Id", context.getFarm().getId())
                        .header("X-Signature", "0".repeat(64))
                        .content(rawBody))
                .andReturn());
    }

    @Cuando("el dispositivo firma un payload y envia otro cuerpo modificado")
    public void firmaPayloadYEnviaCuerpoModificado() throws Exception {
        String signedBody = String.format("{\"deviceId\":\"%s\",\"temperature\":14.5,\"turbidity\":4.0,\"ica\":92.0}", deviceId);
        String signature = ReadingSignatureValidator.computeHmacSha256Hex(context.getFarm().getFarmToken(), signedBody);
        String alteredBody = String.format("{\"deviceId\":\"%s\",\"temperature\":99.9,\"turbidity\":4.0,\"ica\":92.0}", deviceId);

        context.setLastResult(mockMvc.perform(post("/api/v1/telemetry/manual-ingest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Farm-Id", context.getFarm().getId())
                        .header("X-Signature", signature)
                        .content(alteredBody))
                .andReturn());
    }

    @Cuando("se envia una lectura de telemetria sin las cabeceras X-Farm-Id y X-Signature")
    public void enviaLecturaSinCabeceras() throws Exception {
        String rawBody = String.format("{\"deviceId\":\"%s\",\"temperature\":14.5,\"turbidity\":4.0,\"ica\":92.0}", deviceId);

        context.setLastResult(mockMvc.perform(post("/api/v1/telemetry/manual-ingest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(rawBody))
                .andReturn());
    }

    @Entonces("la ingesta responde con codigo {int}")
    public void laIngestaRespondeConCodigo(int expectedStatus) {
        assertEquals(expectedStatus, context.getLastResult().getResponse().getStatus());
    }
}

