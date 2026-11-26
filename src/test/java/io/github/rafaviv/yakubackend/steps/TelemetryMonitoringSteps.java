package io.github.rafaviv.yakubackend.steps;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Dado;
import io.cucumber.java.es.Entonces;
import io.cucumber.java.es.Y;
import io.github.rafaviv.yakubackend.equipment.domain.model.aggregates.Equipment;
import io.github.rafaviv.yakubackend.equipment.domain.model.aggregates.Pond;
import io.github.rafaviv.yakubackend.equipment.domain.model.valueobjects.EquipmentType;
import io.github.rafaviv.yakubackend.equipment.domain.model.valueobjects.Species;
import io.github.rafaviv.yakubackend.equipment.infrastructure.persistence.jpa.repositories.EquipmentRepository;
import io.github.rafaviv.yakubackend.equipment.infrastructure.persistence.jpa.repositories.PondRepository;
import io.github.rafaviv.yakubackend.telemetry.application.internal.ReadingSignatureValidator;
import io.github.rafaviv.yakubackend.telemetry.domain.model.aggregates.MeasurementAggregate;
import io.github.rafaviv.yakubackend.telemetry.domain.model.aggregates.SensorReading;
import io.github.rafaviv.yakubackend.telemetry.domain.model.valueobjects.MeasurementValue;
import io.github.rafaviv.yakubackend.telemetry.domain.model.valueobjects.SensorType;
import io.github.rafaviv.yakubackend.telemetry.infrastructure.persistence.jpa.repositories.MeasurementAggregateRepository;
import io.github.rafaviv.yakubackend.telemetry.infrastructure.persistence.jpa.repositories.SensorReadingRepository;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

public class TelemetryMonitoringSteps {

    private final MockMvc mockMvc;
    private final BddScenarioContext context;
    private final SensorReadingRepository sensorReadingRepository;
    private final MeasurementAggregateRepository measurementAggregateRepository;
    private final EquipmentRepository equipmentRepository;
    private final PondRepository pondRepository;
    private final ObjectMapper objectMapper;

    private Long targetPondId;
    private String linkedDeviceId;

    public TelemetryMonitoringSteps(MockMvc mockMvc,
                                    BddScenarioContext context,
                                    SensorReadingRepository sensorReadingRepository,
                                    MeasurementAggregateRepository measurementAggregateRepository,
                                    EquipmentRepository equipmentRepository,
                                    PondRepository pondRepository,
                                    ObjectMapper objectMapper) {
        this.mockMvc = mockMvc;
        this.context = context;
        this.sensorReadingRepository = sensorReadingRepository;
        this.measurementAggregateRepository = measurementAggregateRepository;
        this.equipmentRepository = equipmentRepository;
        this.pondRepository = pondRepository;
        this.objectMapper = objectMapper;
    }

    @Dado("que el estanque de la granja tiene lecturas recientes registradas")
    public void estanqueTieneLecturasRecientes() {
        targetPondId = context.getPond().getId();
        LocalDateTime now = LocalDateTime.now();
        sensorReadingRepository.save(new SensorReading(targetPondId, SensorType.TEMPERATURE, new MeasurementValue(15.0, "C"), now));
        sensorReadingRepository.save(new SensorReading(targetPondId, SensorType.TURBIDITY, new MeasurementValue(4.0, "NTU"), now));
        sensorReadingRepository.save(new SensorReading(targetPondId, SensorType.ICA, new MeasurementValue(90.0, "INDEX"), now));
    }

    @Cuando("consulta el estado en tiempo real mediante GET \\/api\\/v1\\/telemetry\\/ponds\\/\\{pondId}\\/status")
    public void consultaEstadoEnTiempoReal() throws Exception {
        context.setLastResult(mockMvc.perform(get("/api/v1/telemetry/ponds/{pondId}/status", targetPondId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + context.getAdminJwt()))
                .andReturn());
    }

    @Y("la respuesta de estado incluye lecturas de TEMPERATURE, TURBIDITY e ICA")
    public void respuestaIncluyeLecturas() throws Exception {
        String body = context.getLastResult().getResponse().getContentAsString();
        JsonNode root = objectMapper.readTree(body);
        assertTrue(root.isArray());
        assertTrue(body.contains("TEMPERATURE"));
        assertTrue(body.contains("TURBIDITY"));
        assertTrue(body.contains("ICA"));
    }

    @Dado("que existen agregados historicos recientes para el estanque")
    public void existenAgregadosHistoricos() {
        targetPondId = context.getPond().getId();
        LocalDateTime now = LocalDateTime.now();
        measurementAggregateRepository.save(new MeasurementAggregate(
                targetPondId,
                SensorType.TEMPERATURE,
                12.0,
                16.5,
                14.2,
                now.minusHours(4),
                now.minusHours(1)
        ));
    }

    @Dado("que existe un estanque en la granja sin registros historicos")
    public void existeEstanqueSinHistoricos() {
        Pond emptyPond = pondRepository.save(new Pond(context.getFarm().getId(), "Estanque Vacio", Species.TILAPIA, 250.0));
        targetPondId = emptyPond.getId();
    }

    @Cuando("consulta los datos historicos con filtro {string}")
    public void consultaDatosHistoricosConFiltro(String timeFilter) throws Exception {
        context.setLastResult(mockMvc.perform(get("/api/v1/telemetry/ponds/{pondId}/historical", targetPondId)
                        .param("timeFilter", timeFilter)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + context.getAdminJwt()))
                .andReturn());
    }

    @Y("la respuesta historica incluye valores minimos, maximos y promedios")
    public void respuestaHistoricaIncluyeMinMaxAvg() throws Exception {
        String body = context.getLastResult().getResponse().getContentAsString();
        JsonNode root = objectMapper.readTree(body);
        assertTrue(root.isArray() && !root.isEmpty());
        JsonNode first = root.get(0);
        assertTrue(first.has("minValue"));
        assertTrue(first.has("maxValue"));
        assertTrue(first.has("averageValue"));
    }

    @Y("la lista historica devuelta esta vacia")
    public void listaHistoricaVacia() throws Exception {
        JsonNode root = objectMapper.readTree(context.getLastResult().getResponse().getContentAsString());
        assertTrue(root.isArray());
        assertEquals(0, root.size());
    }

    @Dado("que existe un dispositivo IoT {string} vinculado al estanque")
    public void dispositivoVinculadoAlEstanque(String deviceId) {
        this.linkedDeviceId = deviceId;
        this.targetPondId = context.getPond().getId();
        Equipment eq = new Equipment(EquipmentType.SENSOR, "Sensor Temp", deviceId + "-TEMP", context.getFarm().getId());
        eq.linkToPond(targetPondId);
        equipmentRepository.save(eq);
    }

    @Cuando("envia lecturas agrupadas de temperatura {string}, turbidez {string} e ICA {string} con firma valida")
    public void enviaLecturasAgrupadasConFirmaValida(String tempStr, String turbStr, String icaStr) throws Exception {
        double temp = Double.parseDouble(tempStr);
        double turb = Double.parseDouble(turbStr);
        double ica = Double.parseDouble(icaStr);
        String rawBody = String.format(java.util.Locale.US,
                "{\"deviceId\":\"%s\",\"temperature\":%.2f,\"turbidity\":%.2f,\"ica\":%.2f}",
                linkedDeviceId, temp, turb, ica);
        String signature = ReadingSignatureValidator.computeHmacSha256Hex(context.getFarm().getFarmToken(), rawBody);

        context.setLastResult(mockMvc.perform(post("/api/v1/telemetry/manual-ingest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Farm-Id", context.getFarm().getId())
                        .header("X-Signature", signature)
                        .content(rawBody))
                .andReturn());
    }

    @Y("las lecturas quedan persistidas para el estanque")
    public void lecturasQuedanPersistidas() {
        List<SensorReading> readings = sensorReadingRepository.findByPondIdOrderByTimestampDesc(targetPondId);
        assertFalse(readings.isEmpty());
    }

    @Dado("que el dispositivo {string} no esta vinculado a ningun estanque")
    public void dispositivoNoVinculado(String deviceId) {
        this.linkedDeviceId = deviceId;
    }

    @Cuando("envia lecturas agrupadas con firma valida para {string}")
    public void enviaLecturasParaDispositivo(String deviceId) throws Exception {
        String rawBody = String.format("{\"deviceId\":\"%s\",\"temperature\":21.0,\"turbidity\":3.5,\"ica\":88.0}", deviceId);
        String signature = ReadingSignatureValidator.computeHmacSha256Hex(context.getFarm().getFarmToken(), rawBody);

        context.setLastResult(mockMvc.perform(post("/api/v1/telemetry/manual-ingest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Farm-Id", context.getFarm().getId())
                        .header("X-Signature", signature)
                        .content(rawBody))
                .andReturn());
    }

    @Entonces("la respuesta de telemetria tiene codigo {int}")
    public void respuestaTelemetriaTieneCodigo(int expectedStatus) {
        assertEquals(expectedStatus, context.getLastResult().getResponse().getStatus());
    }
}

