package io.github.rafaviv.yakubackend.telemetry.interfaces.rest;

import io.github.rafaviv.yakubackend.equipment.domain.model.aggregates.Equipment;
import io.github.rafaviv.yakubackend.equipment.domain.model.aggregates.Farm;
import io.github.rafaviv.yakubackend.equipment.domain.model.aggregates.Pond;
import io.github.rafaviv.yakubackend.equipment.domain.model.valueobjects.EquipmentType;
import io.github.rafaviv.yakubackend.equipment.domain.model.valueobjects.Species;
import io.github.rafaviv.yakubackend.equipment.infrastructure.persistence.jpa.repositories.EquipmentRepository;
import io.github.rafaviv.yakubackend.equipment.infrastructure.persistence.jpa.repositories.FarmRepository;
import io.github.rafaviv.yakubackend.equipment.infrastructure.persistence.jpa.repositories.PondRepository;
import io.github.rafaviv.yakubackend.iam.domain.model.aggregates.User;
import io.github.rafaviv.yakubackend.iam.domain.model.entities.Role;
import io.github.rafaviv.yakubackend.iam.domain.model.valueobjects.Email;
import io.github.rafaviv.yakubackend.iam.domain.model.valueobjects.HashedPassword;
import io.github.rafaviv.yakubackend.iam.domain.model.valueobjects.Roles;
import io.github.rafaviv.yakubackend.iam.infrastructure.persistence.jpa.repositories.RoleRepository;
import io.github.rafaviv.yakubackend.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import io.github.rafaviv.yakubackend.iam.infrastructure.tokens.jwt.BearerTokenService;
import io.github.rafaviv.yakubackend.telemetry.application.internal.ReadingSignatureValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb_hmac;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=false;MODE=PostgreSQL",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.mqtt.MqttAutoConfiguration,org.springframework.boot.autoconfigure.integration.IntegrationAutoConfiguration",
        "spring.sql.init.mode=never",
        "mqtt.enabled=false"
})
class TelemetryIngestAndDeviceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FarmRepository farmRepository;

    @Autowired
    private PondRepository pondRepository;

    @Autowired
    private EquipmentRepository equipmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private BearerTokenService tokenService;

    private Farm savedFarm;
    private String adminJwt;

    @BeforeEach
    void setUp() {
        equipmentRepository.deleteAll();
        pondRepository.deleteAll();
        farmRepository.deleteAll();
        userRepository.deleteAll();

        Role adminRole = roleRepository.findByName(Roles.ADMIN)
                .orElseGet(() -> roleRepository.save(new Role(Roles.ADMIN)));

        User admin = new User(
                "hmac_admin",
                new Email("hmac_admin@yaku.com"),
                new HashedPassword("$2a$12$I9jX6Z.By.CDGzZ.ZJ7lhuL2WIdR.qG1Nn6r0I.zE7Lp8X8X8X8X"),
                "Admin",
                "Test",
                true
        );
        admin.addRole(adminRole);
        admin = userRepository.save(admin);

        savedFarm = farmRepository.save(new Farm("Granja HMAC", admin.getId(), "Lima"));
        Pond pond = pondRepository.save(new Pond(savedFarm.getId(), "Estanque 1", Species.TRUCHA, 500.0));

        Equipment tempSensor = new Equipment(EquipmentType.SENSOR, "Sensor Temp", "NODE-01-TEMP", savedFarm.getId());
        tempSensor.linkToPond(pond.getId());
        equipmentRepository.save(tempSensor);

        adminJwt = tokenService.generateToken(admin.getId(), "ADMIN", savedFarm.getId());
    }

    @Test
    @DisplayName("POST /api/v1/telemetry/manual-ingest accepts valid HMAC-SHA256 signature")
    void manualIngestWithValidSignatureReturns200() throws Exception {
        String rawBody = "{\"deviceId\":\"NODE-01\",\"temperature\":15.2,\"turbidity\":4.1,\"ica\":90.0}";
        String signature = ReadingSignatureValidator.computeHmacSha256Hex(savedFarm.getFarmToken(), rawBody);

        mockMvc.perform(post("/api/v1/telemetry/manual-ingest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Farm-Id", savedFarm.getId())
                        .header("X-Signature", signature)
                        .content(rawBody))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/v1/telemetry/manual-ingest rejects missing or invalid signature with 401")
    void manualIngestWithInvalidOrMissingSignatureReturns401() throws Exception {
        String rawBody = "{\"deviceId\":\"NODE-01\",\"temperature\":15.2,\"turbidity\":4.1,\"ica\":90.0}";

        mockMvc.perform(post("/api/v1/telemetry/manual-ingest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Farm-Id", savedFarm.getId())
                        .header("X-Signature", "deadbeef")
                        .content(rawBody))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/v1/telemetry/manual-ingest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(rawBody))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/v1/equipment/iot-device returns 409 when deviceId already exists")
    void registerDuplicateIoTDeviceReturns409() throws Exception {
        String payload = String.format(
                "{\"deviceId\":\"IOT-DUP-01\",\"deviceName\":\"Nodo Prueba\",\"farmId\":%d}",
                savedFarm.getId()
        );

        mockMvc.perform(post("/api/v1/equipment/iot-device")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/equipment/iot-device")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isConflict());
    }
}

