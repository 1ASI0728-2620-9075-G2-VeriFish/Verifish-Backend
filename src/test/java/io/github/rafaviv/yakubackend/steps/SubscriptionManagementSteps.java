package io.github.rafaviv.yakubackend.steps;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Dado;
import io.cucumber.java.es.Entonces;
import io.cucumber.java.es.Y;
import io.github.rafaviv.yakubackend.subscription.application.internal.commandservices.SubscriptionCommandService;
import io.github.rafaviv.yakubackend.subscription.domain.model.aggregates.Subscription;
import io.github.rafaviv.yakubackend.subscription.domain.model.entities.Plan;
import io.github.rafaviv.yakubackend.subscription.domain.model.valueobjects.Currency;
import io.github.rafaviv.yakubackend.subscription.domain.model.valueobjects.SubscriptionStatus;
import io.github.rafaviv.yakubackend.subscription.infrastructure.persistence.jpa.repositories.PlanRepository;
import io.github.rafaviv.yakubackend.subscription.infrastructure.persistence.jpa.repositories.SubscriptionRepository;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

public class SubscriptionManagementSteps {

    private final MockMvc mockMvc;
    private final BddScenarioContext context;
    private final PlanRepository planRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionCommandService subscriptionCommandService;
    private final ObjectMapper objectMapper;

    private Plan targetPlan;

    public SubscriptionManagementSteps(MockMvc mockMvc,
                                       BddScenarioContext context,
                                       PlanRepository planRepository,
                                       SubscriptionRepository subscriptionRepository,
                                       SubscriptionCommandService subscriptionCommandService,
                                       ObjectMapper objectMapper) {
        this.mockMvc = mockMvc;
        this.context = context;
        this.planRepository = planRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.subscriptionCommandService = subscriptionCommandService;
        this.objectMapper = objectMapper;
    }

    @Dado("que existen planes registrados en el sistema")
    public void existenPlanesRegistrados() {
        if (planRepository.findByName("FREE").isEmpty()) {
            planRepository.save(new Plan("FREE", 0.0, Currency.USD, 1, 3650, null));
        }
        if (planRepository.findByName("PREMIUM").isEmpty()) {
            planRepository.save(new Plan("PREMIUM", 19.99, Currency.USD, 10, 30, null));
        }
    }

    @Cuando("consulta los planes mediante GET \\/api\\/v1\\/plans")
    public void consultaLosPlanes() throws Exception {
        context.setLastResult(mockMvc.perform(get("/api/v1/plans")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + context.getAdminJwt()))
                .andReturn());
    }

    @Y("la lista de planes no esta vacia")
    public void listaDePlanesNoVacia() throws Exception {
        JsonNode root = objectMapper.readTree(context.getLastResult().getResponse().getContentAsString());
        assertTrue(root.isArray() && !root.isEmpty());
    }

    @Dado("que existe el plan {string} de {int} dias disponible")
    public void existePlanDisponible(String planName, int durationDays) {
        targetPlan = planRepository.findByName(planName)
                .orElseGet(() -> planRepository.save(new Plan(planName, 19.99, Currency.USD, 10, durationDays, null)));
    }

    @Cuando("confirma la suscripcion del usuario mediante POST \\/api\\/v1\\/subscriptions\\/\\{userId}")
    public void confirmaSuscripcionUsuario() throws Exception {
        String payload = String.format("{\"planId\":%d}", targetPlan.getId());
        context.setLastResult(mockMvc.perform(post("/api/v1/subscriptions/{userId}", context.getAdminUser().getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + context.getAdminJwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andReturn());
    }

    @Y("el estado de la suscripcion devuelta es {string}")
    public void estadoSuscripcionDevueltaEs(String expectedStatus) throws Exception {
        JsonNode node = objectMapper.readTree(context.getLastResult().getResponse().getContentAsString());
        assertEquals(expectedStatus, node.get("status").asText());
    }

    @Y("el periodo de suscripcion corresponde a {int} dias desde hoy")
    public void periodoSuscripcionCorrespondeADias(int days) throws Exception {
        JsonNode node = objectMapper.readTree(context.getLastResult().getResponse().getContentAsString());
        LocalDate start = LocalDate.parse(node.get("startDate").asText());
        LocalDate end = LocalDate.parse(node.get("endDate").asText());
        assertEquals(start.plusDays(days), end);
    }

    @Dado("que el usuario tiene una suscripcion activa al plan {string}")
    public void usuarioTieneSuscripcionActiva(String planName) {
        existenPlanesRegistrados();
        Plan plan = planRepository.findByName(planName).orElseThrow();
        subscriptionCommandService.subscribeUserToPlan(context.getAdminUser().getId(), plan.getId());
    }

    @Cuando("consulta la suscripcion mediante GET \\/api\\/v1\\/subscriptions\\/\\{userId}")
    public void consultaSuscripcionUsuario() throws Exception {
        context.setLastResult(mockMvc.perform(get("/api/v1/subscriptions/{userId}", context.getAdminUser().getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + context.getAdminJwt()))
                .andReturn());
    }

    @Y("la respuesta incluye el nombre del plan y estado {string}")
    public void respuestaIncluyePlanYEstado(String expectedStatus) throws Exception {
        JsonNode node = objectMapper.readTree(context.getLastResult().getResponse().getContentAsString());
        assertTrue(node.has("planName") && !node.get("planName").asText().isBlank());
        assertEquals(expectedStatus, node.get("status").asText());
    }

    @Cuando("cancela la suscripcion mediante DELETE \\/api\\/v1\\/subscriptions\\/\\{userId}")
    public void cancelaSuscripcion() throws Exception {
        context.setLastResult(mockMvc.perform(delete("/api/v1/subscriptions/{userId}", context.getAdminUser().getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + context.getAdminJwt()))
                .andReturn());
    }

    @Y("el estado persistido de la suscripcion cambia a {string}")
    public void estadoPersistidoCambiaA(String expectedStatus) {
        Subscription sub = subscriptionRepository.findByUserId(context.getAdminUser().getId()).orElseThrow();
        assertEquals(SubscriptionStatus.valueOf(expectedStatus), sub.getStatus());
    }

    @Cuando("intenta suscribir al usuario a un plan inexistente con ID {int}")
    public void intentaSuscribirPlanInexistente(int nonexistentPlanId) throws Exception {
        String payload = String.format("{\"planId\":%d}", nonexistentPlanId);
        context.setLastResult(mockMvc.perform(post("/api/v1/subscriptions/{userId}", context.getAdminUser().getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + context.getAdminJwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andReturn());
    }

    @Entonces("el modulo de suscripciones responde con codigo {int}")
    public void moduloSuscripcionesRespondeConCodigo(int expectedStatus) {
        assertEquals(expectedStatus, context.getLastResult().getResponse().getStatus());
    }
}

