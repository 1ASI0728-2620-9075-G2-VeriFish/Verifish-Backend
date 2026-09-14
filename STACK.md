# Yaku Backend — Stack Técnico y Guía de Ejecución

## 1. Información General del Proyecto
- **Tipo de Arquitectura**: Monolito Modular (Domain-Driven Design / Clean Architecture con Spring Modulith).
- **Lenguaje**: Java 17.
- **Framework Core**: Spring Boot 3.4.3 / Spring Modulith 1.3.1.
- **Build Tool**: Maven 3.9+ (`mvnw` incluido).

---

## 2. Dependencias y Decisiones Arquitectónicas Clave

| Componente | Librería / Tecnología | Versión | Propósito / Decisión |
|---|---|---|---|
| **Arquitectura Modular** | `spring-modulith-starter-core`, `spring-modulith-starter-jpa` | 1.3.1 | Encapsulamiento de módulos (IAM, Subscriptions, Equipment, Farms, Telemetry, Notifications) y desacoplamiento de eventos. |
| **Persistencia & ORM** | `spring-boot-starter-data-jpa`, Hibernate | Spring Boot 3.4.3 | Mapeo objeto-relacional y repositorios JPA. |
| **Base de Datos** | PostgreSQL Driver (`org.postgresql:postgresql`) / H2 (testing) | Runtime | Base de datos relacional principal. |
| **Seguridad & Auth** | `spring-boot-starter-security`, `jjwt` (Java JWT API + Jackson) | 0.12.6 | Autenticación basada en Bearer Tokens JWT sin estado (stateless). |
| **Pasarela de Pago** | Stripe Java SDK (`stripe-java`) | 24.22.0 | Creación de Payment Intents, Checkout Sessions de suscripciones y recepción de webhooks. |
| **Notificaciones Push** | Google Firebase Admin SDK (`firebase-admin`) | 9.4.2 | Envío de notificaciones push vía Firebase Cloud Messaging (FCM). |
| **Ingesta IoT / Mensajería** | `spring-integration-mqtt` (Eclipse Paho) | Spring Boot 3.4.3 | Suscripción a tópicos MQTT para recibir telemetría de estanques acuícolas. |
| **Documentación API** | `springdoc-openapi-starter-webmvc-ui` (Swagger OpenAPI 3) | 2.8.8 | Generación interactiva de documentación Swagger en `/swagger-ui.html`. |
| **Productividad** | Project Lombok | Managed | Reducción de boilerplate (`@Getter`, `@Setter`, `@Builder`, `@RequiredArgsConstructor`). |
| **Testing** | JUnit 5 (`junit-jupiter`), Spring Boot Test, H2 In-Memory | 5.11.4 | Pruebas unitarias y de integración modular. |

---

## 3. Servicios Externos de Terceros

1. **Stripe API**: Gestión de pagos y suscripciones de usuarios a planes de monitoreo acuícola.
2. **Firebase Cloud Messaging (FCM)**: Notificaciones automáticas de alertas críticas (e.g., variaciones de pH, temperatura fuera de rango).
3. **MQTT Broker (e.g. Eclipse Mosquitto)**: Broker de mensajería para comunicación con nodos de borde (Yaku Edge) y sensores IoT.
4. **PostgreSQL Database**: Servidor de base de datos relacional (local vía Docker o en la nube).

---

## 4. Cómo Levantar el Proyecto Localmente

### Prerrequisitos
- JDK 17 o superior instalado y configurado en `JAVA_HOME`.
- Docker y Docker Compose (opcional para base de datos y broker MQTT).

### Pasos de Configuración
1. **Configurar variables de entorno:**
   ```bash
   cp .env.example .env
   # Edita .env con tus credenciales locales o valores por defecto
   ```

2. **Levantar base de datos PostgreSQL local (vía Docker Compose):**
   ```bash
   docker compose up -d postgres
   ```

3. **Compilar el proyecto:**
   ```bash
   # En Windows
   .\mvnw.cmd clean compile

   # En Linux / macOS
   ./mvnw clean compile
   ```

4. **Ejecutar pruebas:**
   ```bash
   .\mvnw.cmd test
   ```

5. **Iniciar la aplicación:**
   ```bash
   .\mvnw.cmd spring-boot:run
   ```

6. **Acceder a la documentación Swagger UI:**
   - URL: `http://localhost:8080/swagger-ui.html`
   - OpenAPI Docs JSON: `http://localhost:8080/v3/api-docs`
