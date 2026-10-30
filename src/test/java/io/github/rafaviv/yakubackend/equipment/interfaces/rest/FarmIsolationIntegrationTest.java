package io.github.rafaviv.yakubackend.equipment.interfaces.rest;

import io.github.rafaviv.yakubackend.equipment.domain.model.aggregates.Pond;
import io.github.rafaviv.yakubackend.equipment.domain.model.valueobjects.Species;
import io.github.rafaviv.yakubackend.equipment.infrastructure.persistence.jpa.repositories.PondRepository;
import io.github.rafaviv.yakubackend.iam.domain.model.aggregates.User;
import io.github.rafaviv.yakubackend.iam.domain.model.entities.Role;
import io.github.rafaviv.yakubackend.iam.domain.model.valueobjects.Email;
import io.github.rafaviv.yakubackend.iam.domain.model.valueobjects.HashedPassword;
import io.github.rafaviv.yakubackend.iam.domain.model.valueobjects.Roles;
import io.github.rafaviv.yakubackend.iam.infrastructure.persistence.jpa.repositories.RoleRepository;
import io.github.rafaviv.yakubackend.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import io.github.rafaviv.yakubackend.iam.infrastructure.tokens.jwt.BearerTokenService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=false;MODE=PostgreSQL;NON_KEYWORDS=VALUE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.mqtt.MqttAutoConfiguration,org.springframework.boot.autoconfigure.integration.IntegrationAutoConfiguration",
        "spring.sql.init.mode=never",
        "mqtt.enabled=false"
})
class FarmIsolationIntegrationTest {

    private static final Long FARM_A_ID = 101L;
    private static final Long FARM_B_ID = 202L;
    private static final String SECRET_POND_NAME = "Pond-Confidencial-Granja-101";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PondRepository pondRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private BearerTokenService tokenService;

    @Value("${authorization.jwt.secret}")
    private String jwtSecret;

    private User farmAUser;
    private User farmBUser;
    private User noFarmUser;
    private Pond farmAPond;

    @BeforeEach
    void setUp() {
        pondRepository.deleteAll();
        userRepository.deleteAll();

        Role adminRole = roleRepository.findByName(Roles.ADMIN)
                .orElseGet(() -> roleRepository.save(new Role(Roles.ADMIN)));

        farmAUser = new User(
                "admin_farm_a",
                new Email("admin_a@verifish.test"),
                new HashedPassword("hashedPassA123"),
                "Admin",
                "FarmA",
                true
        );
        farmAUser.addRole(adminRole);
        farmAUser.setFarmId(FARM_A_ID);
        farmAUser = userRepository.save(farmAUser);

        farmBUser = new User(
                "admin_farm_b",
                new Email("admin_b@verifish.test"),
                new HashedPassword("hashedPassB123"),
                "Admin",
                "FarmB",
                true
        );
        farmBUser.addRole(adminRole);
        farmBUser.setFarmId(FARM_B_ID);
        farmBUser = userRepository.save(farmBUser);

        noFarmUser = new User(
                "admin_no_farm",
                new Email("admin_nofarm@verifish.test"),
                new HashedPassword("hashedPassNoFarm123"),
                "Admin",
                "NoFarm",
                true
        );
        noFarmUser.addRole(adminRole);
        noFarmUser = userRepository.save(noFarmUser);

        farmAPond = pondRepository.save(new Pond(FARM_A_ID, SECRET_POND_NAME, Species.TRUCHA, 250.5));
    }

    @Test
    @DisplayName("GET /api/v1/ponds/{id} with valid token of same farm returns 200 OK")
    void getPondById_WithValidTokenSameFarm_Returns200() throws Exception {
        String token = tokenService.generateToken(farmAUser.getId(), "ADMIN", FARM_A_ID);

        mockMvc.perform(get("/api/v1/ponds/{id}", farmAPond.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(farmAPond.getId()))
                .andExpect(jsonPath("$.name").value(SECRET_POND_NAME))
                .andExpect(jsonPath("$.farmId").value(FARM_A_ID));
    }

    @Test
    @DisplayName("GET /api/v1/ponds/{id} without token returns 401 Unauthorized")
    void getPondById_WithoutToken_Returns401() throws Exception {
        mockMvc.perform(get("/api/v1/ponds/{id}", farmAPond.getId()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/v1/ponds/{id} with expired token returns 401 Unauthorized")
    void getPondById_WithExpiredToken_Returns401() throws Exception {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        Date pastIssuedAt = new Date(System.currentTimeMillis() - 7200_000L);
        Date pastExpiration = new Date(System.currentTimeMillis() - 3600_000L);

        String expiredToken = Jwts.builder()
                .subject(farmAUser.getId().toString())
                .claim("role", "ADMIN")
                .claim("farmId", FARM_A_ID)
                .issuedAt(pastIssuedAt)
                .expiration(pastExpiration)
                .signWith(key)
                .compact();

        mockMvc.perform(get("/api/v1/ponds/{id}", farmAPond.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + expiredToken))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(""));
    }

    @Test
    @DisplayName("GET /api/v1/ponds/{id} with invalid signature token returns 401 Unauthorized")
    void getPondById_WithInvalidSignatureToken_Returns401() throws Exception {
        String wrongSecret = "AnotherCompletelyDifferentSecretKeyWithAtLeast256BitsForHmacSha";
        SecretKey wrongKey = Keys.hmacShaKeyFor(wrongSecret.getBytes(StandardCharsets.UTF_8));
        Date now = new Date();
        Date future = new Date(now.getTime() + 3600_000L);

        String invalidSigToken = Jwts.builder()
                .subject(farmAUser.getId().toString())
                .claim("role", "ADMIN")
                .claim("farmId", FARM_A_ID)
                .issuedAt(now)
                .expiration(future)
                .signWith(wrongKey)
                .compact();

        mockMvc.perform(get("/api/v1/ponds/{id}", farmAPond.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + invalidSigToken))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(""));
    }

    @Test
    @DisplayName("GET /api/v1/ponds/{id} with valid token of another farm returns 403 Forbidden without leaking resource data")
    void getPondById_WithValidTokenDifferentFarm_Returns403WithoutResourceData() throws Exception {
        String otherFarmToken = tokenService.generateToken(farmBUser.getId(), "ADMIN", FARM_B_ID);

        mockMvc.perform(get("/api/v1/ponds/{id}", farmAPond.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + otherFarmToken))
                .andExpect(status().isForbidden())
                .andExpect(content().string(not(containsString(SECRET_POND_NAME))))
                .andExpect(content().string(not(containsString("TRUCHA"))))
                .andExpect(content().string(not(containsString("250.5"))))
                .andExpect(content().string(not(containsString(FARM_A_ID.toString()))));
    }

    @Test
    @DisplayName("GET /api/v1/ponds/{id} with valid token having null farmId returns 403 Forbidden without leaking resource data")
    void getPondById_WithValidTokenNullFarmId_Returns403WithoutResourceData() throws Exception {
        String nullFarmToken = tokenService.generateToken(noFarmUser.getId(), "ADMIN", null);

        mockMvc.perform(get("/api/v1/ponds/{id}", farmAPond.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + nullFarmToken))
                .andExpect(status().isForbidden())
                .andExpect(content().string(not(containsString(SECRET_POND_NAME))))
                .andExpect(content().string(not(containsString("TRUCHA"))));
    }
}

