package io.github.rafaviv.yakubackend.steps;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Dado;
import io.cucumber.java.es.Entonces;
import io.cucumber.java.es.Y;
import io.github.rafaviv.yakubackend.equipment.domain.model.aggregates.Equipment;
import io.github.rafaviv.yakubackend.equipment.domain.model.valueobjects.EquipmentType;
import io.github.rafaviv.yakubackend.equipment.infrastructure.persistence.jpa.repositories.EquipmentRepository;
import io.github.rafaviv.yakubackend.equipment.infrastructure.persistence.jpa.repositories.PondRepository;
import io.github.rafaviv.yakubackend.iam.domain.model.aggregates.User;
import io.github.rafaviv.yakubackend.iam.domain.model.entities.Role;
import io.github.rafaviv.yakubackend.iam.domain.model.valueobjects.Email;
import io.github.rafaviv.yakubackend.iam.domain.model.valueobjects.HashedPassword;
import io.github.rafaviv.yakubackend.iam.domain.model.valueobjects.Roles;
import io.github.rafaviv.yakubackend.iam.infrastructure.persistence.jpa.repositories.RoleRepository;
import io.github.rafaviv.yakubackend.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import io.github.rafaviv.yakubackend.notification.domain.models.valueobjects.NotificationType;
import io.github.rafaviv.yakubackend.notification.infrastructure.persistance.jpa.NotificationEntity;
import io.github.rafaviv.yakubackend.notification.infrastructure.persistance.jpa.NotificationJpaRepository;
import io.github.rafaviv.yakubackend.telemetry.application.internal.ReadingSignatureValidator;
import io.github.rafaviv.yakubackend.telemetry.domain.model.aggregates.Threshold;
import io.github.rafaviv.yakubackend.telemetry.domain.model.valueobjects.Species;
import io.github.rafaviv.yakubackend.telemetry.infrastructure.persistence.jpa.repositories.ThresholdRepository;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

public class AlertSystemSteps {

    private final MockMvc mockMvc;
    private final BddScenarioContext context;
    private final ThresholdRepository thresholdRepository;
    private final EquipmentRepository equipmentRepository;
    private final PondRepository pondRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final NotificationJpaRepository notificationJpaRepository;
    private final ObjectMapper objectMapper;

    private String thresholdPayload;

    public AlertSystemSteps(MockMvc mockMvc,
                            BddScenarioContext context,
                            ThresholdRepository thresholdRepository,
                            EquipmentRepository equipmentRepository,
                            PondRepository pondRepository,
                            UserRepository userRepository,
                            RoleRepository roleRepository,
                            NotificationJpaRepository notificationJpaRepository,
                            ObjectMapper objectMapper) {
        this.mockMvc = mockMvc;
        this.context = context;
        this.thresholdRepository = thresholdRepository;
        this.equipmentRepository = equipmentRepository;
        this.pondRepository = pondRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.notificationJpaRepository = notificationJpaRepository;
        this.objectMapper = objectMapper;
    }

    @Dado("que existe un estanque de especie {string} con umbrales configurados y dispositivo {string} vinculado")
    public void estanqueConUmbralesYDispositivo(String speciesStr, String deviceId) {
        Species species = Species.valueOf(speciesStr.toUpperCase());
        if (!thresholdRepository.existsBySpecies(species)) {
            thresholdRepository.save(new Threshold(species, 10.0, 18.0, 3.5, 4.5));
        }

        Role opRole = roleRepository.findByName(Roles.OPERATOR)
                .orElseGet(() -> roleRepository.save(new Role(Roles.OPERATOR)));
        User operator = new User(
                "bdd_operator",
                new Email("bdd_operator@yaku.com"),
                new HashedPassword("$2a$12$I9jX6Z.By.CDGzZ.ZJ7lhuL2WIdR.qG1Nn6r0I.zE7Lp8X8X8X8X"),
                "Operador",
                "BDD",
                true
        );
        operator.addRole(opRole);
        operator.setAssignedFarmId(context.getFarm().getId());
        operator = userRepository.save(operator);
        context.setOperatorUser(operator);

        var pond = context.getPond();
        pond.assignOperator(operator.getId());
        pondRepository.save(pond);

        Equipment sensor = new Equipment(EquipmentType.SENSOR, "Sensor Temp", deviceId + "-TEMP", context.getFarm().getId());
        sensor.linkToPond(pond.getId());
        equipmentRepository.save(sensor);
    }

    @Cuando("llega una lectura con temperatura {string} y turbidez {string} para {string}")
    public void llegaLecturaParaDispositivo(String tempStr, String turbStr, String deviceId) throws Exception {
        double temperature = Double.parseDouble(tempStr);
        double turbidity = Double.parseDouble(turbStr);
        String rawBody = String.format(java.util.Locale.US,
                "{\"deviceId\":\"%s\",\"temperature\":%.2f,\"turbidity\":%.2f,\"ica\":60.0}",
                deviceId, temperature, turbidity);
        String signature = ReadingSignatureValidator.computeHmacSha256Hex(context.getFarm().getFarmToken(), rawBody);

        context.setLastResult(mockMvc.perform(post("/api/v1/telemetry/manual-ingest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Farm-Id", context.getFarm().getId())
                        .header("X-Signature", signature)
                        .content(rawBody))
                .andReturn());
        assertEquals(200, context.getLastResult().getResponse().getStatus());
    }

    @Entonces("se registra una notificacion de tipo {string} para el operador del estanque")
    public void notificacionRegistradaParaOperador(String expectedType) {
        List<NotificationEntity> notifs = notificationJpaRepository
                .findByRecipientUserIdOrderByCreatedAtDesc(context.getOperatorUser().getId());
        assertFalse(notifs.isEmpty());
        assertEquals(expectedType, notifs.get(0).getType().name());
    }

    @Entonces("se registra una notificacion de tipo {string} para el administrador y el operador")
    public void notificacionRegistradaParaAdminYOperador(String expectedType) {
        List<NotificationEntity> adminNotifs = notificationJpaRepository
                .findByRecipientUserIdOrderByCreatedAtDesc(context.getAdminUser().getId());
        List<NotificationEntity> opNotifs = notificationJpaRepository
                .findByRecipientUserIdOrderByCreatedAtDesc(context.getOperatorUser().getId());

        assertFalse(adminNotifs.isEmpty());
        assertFalse(opNotifs.isEmpty());
        assertEquals(expectedType, adminNotifs.get(0).getType().name());
        assertEquals(expectedType, opNotifs.get(0).getType().name());
    }

    @Dado("que el administrador prepara umbrales validos para la especie {string}")
    public void preparaUmbralesValidos(String species) {
        thresholdPayload = String.format(
                "{\"species\":\"%s\",\"minTemperature\":18.0,\"maxTemperature\":28.0,\"minTurbidity\":1.0,\"maxTurbidity\":15.0}",
                species
        );
    }

    @Cuando("guarda la configuracion mediante POST \\/api\\/v1\\/telemetry\\/thresholds")
    public void guardaConfiguracionUmbrales() throws Exception {
        context.setLastResult(mockMvc.perform(post("/api/v1/telemetry/thresholds")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + context.getAdminJwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(thresholdPayload))
                .andReturn());
    }

    @Y("el umbral de la especie {string} puede consultarse mediante GET \\/api\\/v1\\/telemetry\\/thresholds\\/OTRO")
    public void consultaUmbralPorEspecie(String species) throws Exception {
        var res = mockMvc.perform(get("/api/v1/telemetry/thresholds/{species}", species)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + context.getAdminJwt()))
                .andReturn();
        assertEquals(200, res.getResponse().getStatus());
        assertTrue(res.getResponse().getContentAsString().contains(species));
    }

    @Dado("que el usuario autenticado accede desde su dispositivo movil")
    public void usuarioAccedeDesdeDispositivoMovil() {
        assertNotNull(context.getAdminUser());
    }

    @Cuando("registra su token FCM mediante POST \\/api\\/v1\\/users\\/\\{userId}\\/device-tokens")
    public void registraTokenFcm() throws Exception {
        context.setLastResult(mockMvc.perform(post("/api/v1/users/{userId}/device-tokens", context.getAdminUser().getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + context.getAdminJwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fcmToken\":\"fcm-test-token-123\"}"))
                .andReturn());
    }

    @Dado("que el usuario autenticado tiene notificaciones registradas")
    public void usuarioTieneNotificacionesRegistradas() {
        NotificationEntity entity = new NotificationEntity();
        entity.setType(NotificationType.WARNING);
        entity.setMessage("Temperatura elevada en Estanque BDD");
        entity.setRecipientUserId(context.getAdminUser().getId());
        entity.setCreatedAt(LocalDateTime.now());
        entity.setRead(false);
        notificationJpaRepository.save(entity);
    }

    @Cuando("consulta mediante GET \\/api\\/v1\\/users\\/\\{userId}\\/notifications")
    public void consultaNotificaciones() throws Exception {
        context.setLastResult(mockMvc.perform(get("/api/v1/users/{userId}/notifications", context.getAdminUser().getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + context.getAdminJwt()))
                .andReturn());
    }

    @Y("cada notificacion incluye tipo, mensaje, destinatario y fecha de creacion")
    public void notificacionIncluyeCampos() throws Exception {
        JsonNode root = objectMapper.readTree(context.getLastResult().getResponse().getContentAsString());
        assertTrue(root.isArray() && !root.isEmpty());
        JsonNode first = root.get(0);
        assertTrue(first.has("type"));
        assertTrue(first.has("message"));
        assertTrue(first.has("recipientUserId"));
        assertTrue(first.has("createdAt"));
    }

    @Dado("que un sistema externo envia una alerta por webhook")
    public void sistemaExternoEnviaAlertaWebhook() {
        assertNotNull(context.getAdminUser());
    }

    @Cuando("se recibe mediante POST \\/api\\/v1\\/webhooks\\/notifications")
    public void recibeWebhookNotificacion() throws Exception {
        String payload = String.format(
                "{\"type\":\"CRITICAL\",\"message\":\"Fallo de bomba\",\"userId\":%d,\"value\":35.0,\"sensorType\":\"TEMPERATURE\",\"hardwareStatus\":\"OFFLINE\"}",
                context.getAdminUser().getId()
        );
        context.setLastResult(mockMvc.perform(post("/api/v1/webhooks/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andReturn());
    }

    @Y("se genera una notificacion interna para el usuario")
    public void generaNotificacionInterna() {
        List<NotificationEntity> notifs = notificationJpaRepository
                .findByRecipientUserIdOrderByCreatedAtDesc(context.getAdminUser().getId());
        assertFalse(notifs.isEmpty());
        assertEquals(NotificationType.CRITICAL, notifs.get(0).getType());
    }

    @Entonces("el modulo de alertas responde con codigo {int}")
    public void moduloAlertasRespondeConCodigo(int expectedStatus) {
        assertEquals(expectedStatus, context.getLastResult().getResponse().getStatus());
    }
}

