package io.github.rafaviv.yakubackend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=false;MODE=PostgreSQL",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.mqtt.MqttAutoConfiguration,org.springframework.boot.autoconfigure.integration.IntegrationAutoConfiguration",
        "spring.sql.init.mode=never",
        "mqtt.enabled=false"
})
class YakuBackendApplicationTests {

    @Test
    void contextLoads() {
    }

}
