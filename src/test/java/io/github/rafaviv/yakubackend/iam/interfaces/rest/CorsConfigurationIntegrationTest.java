package io.github.rafaviv.yakubackend.iam.interfaces.rest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:corstestdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=false;MODE=PostgreSQL;NON_KEYWORDS=VALUE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.mqtt.MqttAutoConfiguration,org.springframework.boot.autoconfigure.integration.IntegrationAutoConfiguration",
        "spring.sql.init.mode=never",
        "mqtt.enabled=false",
        "app.cors.allowed-origins= https://yakucontrol-v2.web.app , https://yakucontrol-v2.firebaseapp.com ,,"
})
class CorsConfigurationIntegrationTest {

    private static final String SIGNIN = "/api/v1/users/signin";
    private static final String ALLOWED = "https://yakucontrol-v2.web.app";
    private static final String ALLOWED_2 = "https://yakucontrol-v2.firebaseapp.com";

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Preflight from a configured origin is allowed and echoes the origin")
    void preflightAllowedOrigin() throws Exception {
        mockMvc.perform(options(SIGNIN)
                        .header("Origin", ALLOWED)
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "content-type,authorization"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", ALLOWED))
                .andExpect(header().string("Access-Control-Allow-Credentials", "true"));
    }

    @Test
    @DisplayName("Second comma-separated origin (with surrounding spaces) is also allowed")
    void preflightSecondOrigin() throws Exception {
        mockMvc.perform(options(SIGNIN)
                        .header("Origin", ALLOWED_2)
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", ALLOWED_2));
    }

    @Test
    @DisplayName("Preflight from an unlisted origin is rejected with 403")
    void preflightUnlistedOrigin() throws Exception {
        mockMvc.perform(options(SIGNIN)
                        .header("Origin", "https://evil.example")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Fixed patterns still work (localhost)")
    void preflightLocalhostStillAllowed() throws Exception {
        mockMvc.perform(options(SIGNIN)
                        .header("Origin", "http://localhost:3000")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:3000"));
    }

    @Test
    @DisplayName("401 from the entry point still carries CORS headers")
    void unauthorizedResponseHasCorsHeaders() throws Exception {
        mockMvc.perform(get("/api/v1/ponds").header("Origin", ALLOWED))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("Access-Control-Allow-Origin", ALLOWED));
    }

    @Test
    @DisplayName("401 from the JWT filter (invalid token) still carries CORS headers")
    void invalidTokenResponseHasCorsHeaders() throws Exception {
        mockMvc.perform(get("/api/v1/ponds")
                        .header("Origin", ALLOWED)
                        .header("Authorization", "Bearer not.a.valid-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("Access-Control-Allow-Origin", ALLOWED));
    }
}
