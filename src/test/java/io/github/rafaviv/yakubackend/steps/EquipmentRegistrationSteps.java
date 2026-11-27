package io.github.rafaviv.yakubackend.steps;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Dado;
import io.cucumber.java.es.Entonces;
import io.cucumber.java.es.Y;
import io.github.rafaviv.yakubackend.equipment.domain.model.aggregates.Equipment;
import io.github.rafaviv.yakubackend.equipment.domain.model.valueobjects.EquipmentType;
import io.github.rafaviv.yakubackend.equipment.domain.services.EquipmentCommandService;
import io.github.rafaviv.yakubackend.equipment.infrastructure.persistence.jpa.repositories.EquipmentRepository;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

public class EquipmentRegistrationSteps {

    private final MockMvc mockMvc;
    private final BddScenarioContext context;
    private final EquipmentRepository equipmentRepository;
    private final EquipmentCommandService equipmentCommandService;
    private final ObjectMapper objectMapper;

    private Equipment targetEquipment;

    public EquipmentRegistrationSteps(MockMvc mockMvc,
                                      BddScenarioContext context,
                                      EquipmentRepository equipmentRepository,
                                      EquipmentCommandService equipmentCommandService,
                                      ObjectMapper objectMapper) {
        this.mockMvc = mockMvc;
        this.context = context;
        this.equipmentRepository = equipmentRepository;
        this.equipmentCommandService = equipmentCommandService;
        this.objectMapper = objectMapper;
    }

    @Cuando("registro un equipo {string} con nombre {string} y codigo fisico {string} mediante POST \\/api\\/v1\\/equipment")
    public void registroEquipoIndividual(String type, String name, String physicalCode) throws Exception {
        String payload = String.format(
                "{\"type\":\"%s\",\"name\":\"%s\",\"physicalCode\":\"%s\",\"farmId\":%d}",
                type, name, physicalCode, context.getFarm().getId()
        );
        context.setLastResult(mockMvc.perform(post("/api/v1/equipment")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + context.getAdminJwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andReturn());
    }

    @Y("el equipo registrado tiene estado inicial {string}")
    public void equipoTieneEstadoInicial(String expectedStatus) throws Exception {
        JsonNode node = objectMapper.readTree(context.getLastResult().getResponse().getContentAsString());
        assertEquals(expectedStatus, node.get("status").asText());
    }

    @Cuando("registro un nodo IoT con deviceId {string} y nombre {string} mediante POST \\/api\\/v1\\/equipment\\/iot-device")
    public void registroNodoIoT(String deviceId, String deviceName) throws Exception {
        String payload = String.format(
                "{\"deviceId\":\"%s\",\"deviceName\":\"%s\",\"farmId\":%d}",
                deviceId, deviceName, context.getFarm().getId()
        );
        context.setLastResult(mockMvc.perform(post("/api/v1/equipment/iot-device")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + context.getAdminJwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andReturn());
    }

    @Y("se crean los 4 componentes del nodo IoT en estado {string}")
    public void seCrean4Componentes(String expectedStatus) throws Exception {
        JsonNode root = objectMapper.readTree(context.getLastResult().getResponse().getContentAsString());
        assertTrue(root.isArray());
        assertEquals(4, root.size());
        for (JsonNode item : root) {
            assertEquals(expectedStatus, item.get("status").asText());
        }
    }

    @Dado("que ya existe un nodo IoT registrado con deviceId {string}")
    public void yaExisteNodoIoT(String deviceId) {
        equipmentCommandService.registerIoTDevice(deviceId, "Nodo Existente", context.getFarm().getId());
    }

    @Cuando("intento registrar nuevamente el nodo IoT con deviceId {string} mediante POST \\/api\\/v1\\/equipment\\/iot-device")
    public void intentoRegistrarNodoDuplicado(String deviceId) throws Exception {
        registroNodoIoT(deviceId, "Nodo Duplicado");
    }

    @Dado("que existe un equipo disponible en la granja y un estanque disponible")
    public void existeEquipoYEstanqueDisponible() {
        targetEquipment = equipmentRepository.save(
                new Equipment(EquipmentType.SENSOR, "Sensor Link", "SL-100", context.getFarm().getId())
        );
    }

    @Cuando("vinculo el equipo al estanque mediante POST \\/api\\/v1\\/equipment\\/\\{equipmentId}\\/link\\/\\{pondId}")
    public void vinculoEquipoAlEstanque() throws Exception {
        context.setLastResult(mockMvc.perform(post("/api/v1/equipment/{equipmentId}/link/{pondId}",
                        targetEquipment.getId(), context.getPond().getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + context.getAdminJwt()))
                .andReturn());
    }

    @Y("el equipo cambia su estado a {string} con el pondId correspondiente")
    public void equipoCambiaEstadoALinked(String expectedStatus) throws Exception {
        JsonNode node = objectMapper.readTree(context.getLastResult().getResponse().getContentAsString());
        assertEquals(expectedStatus, node.get("status").asText());
        assertEquals(context.getPond().getId().longValue(), node.get("pondId").asLong());
    }

    @Dado("que existen equipos registrados en la granja")
    public void existenEquiposRegistrados() {
        equipmentRepository.save(
                new Equipment(EquipmentType.ACTUATOR, "Bomba Principal", "BP-01", context.getFarm().getId())
        );
    }

    @Cuando("consulto los equipos mediante GET \\/api\\/v1\\/equipment")
    public void consultoEquipos() throws Exception {
        context.setLastResult(mockMvc.perform(get("/api/v1/equipment")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + context.getAdminJwt()))
                .andReturn());
    }

    @Y("la lista contiene los equipos de la granja")
    public void listaContieneEquipos() throws Exception {
        JsonNode root = objectMapper.readTree(context.getLastResult().getResponse().getContentAsString());
        assertTrue(root.isArray() && !root.isEmpty());
    }

    @Dado("que existe un equipo disponible en la granja")
    public void existeEquipoDisponible() {
        targetEquipment = equipmentRepository.save(
                new Equipment(EquipmentType.SENSOR, "Sensor Borrar", "DEL-01", context.getFarm().getId())
        );
    }

    @Cuando("elimino el equipo mediante DELETE \\/api\\/v1\\/equipment\\/\\{id}")
    public void eliminoEquipo() throws Exception {
        context.setLastResult(mockMvc.perform(delete("/api/v1/equipment/{id}", targetEquipment.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + context.getAdminJwt()))
                .andReturn());
    }

    @Entonces("el modulo de equipos responde con codigo {int}")
    public void moduloEquiposRespondeConCodigo(int expectedStatus) {
        assertEquals(expectedStatus, context.getLastResult().getResponse().getStatus());
    }
}

