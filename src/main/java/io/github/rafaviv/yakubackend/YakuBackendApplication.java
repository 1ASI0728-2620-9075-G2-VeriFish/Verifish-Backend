package io.github.rafaviv.yakubackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class YakuBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(YakuBackendApplication.class, args);
    }

}
