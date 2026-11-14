package io.github.rafaviv.yakubackend.iam.infrastructure.authorization.sfs.configuration;

import io.github.rafaviv.yakubackend.iam.infrastructure.tokens.jwt.BearerTokenService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import io.github.rafaviv.yakubackend.iam.infrastructure.authorization.sfs.model.UserDetailsServiceExtension;
import io.github.rafaviv.yakubackend.iam.infrastructure.authorization.sfs.pipeline.BearerAuthorizationRequestFilter;
import io.github.rafaviv.yakubackend.iam.infrastructure.hashing.bcrypt.BCryptHashingService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Web Security Configuration.
 * Configura la seguridad de la API.
 */
@Configuration
@EnableMethodSecurity
public class WebSecurityConfiguration {

    private final UserDetailsServiceExtension userDetailsService;
    private final BearerTokenService tokenService;
    private final BCryptHashingService hashingService;
    private final AuthenticationEntryPoint unauthorizedRequestHandler;
    private final List<String> configuredOrigins;

    public WebSecurityConfiguration(
            @Qualifier("defaultUserDetailsService") UserDetailsServiceExtension userDetailsService,
            BearerTokenService tokenService,
            BCryptHashingService hashingService,
            AuthenticationEntryPoint authenticationEntryPoint,
            @Value("${app.cors.allowed-origins:}") String allowedOrigins) {
        this.userDetailsService = userDetailsService;
        this.tokenService = tokenService;
        this.hashingService = hashingService;
        this.unauthorizedRequestHandler = authenticationEntryPoint;
        this.configuredOrigins = parseOrigins(allowedOrigins);
    }

    /** Splits a comma-separated origin list, trimming entries and dropping blanks and bare wildcards. */
    static List<String> parseOrigins(String raw) {
        if (raw == null) {
            return List.of();
        }
        return Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty() && !s.equals("*"))
                .toList();
    }

    @Bean
    public BearerAuthorizationRequestFilter authorizationRequestFilter() {
        return new BearerAuthorizationRequestFilter(tokenService, userDetailsService);
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(hashingService);
        return provider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return hashingService;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(request -> {
                    var corsConfig = new CorsConfiguration();
                    var patterns = new ArrayList<>(List.of(
                            "http://localhost:*",
                            "http://127.0.0.1:*",
                            "https://*.azurestaticapps.net",
                            "https://*.web.core.windows.net",
                            "https://*.z*.web.core.windows.net"));
                    configuredOrigins.stream().filter(o -> !patterns.contains(o)).forEach(patterns::add);
                    corsConfig.setAllowedOriginPatterns(patterns);
                    corsConfig.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
                    corsConfig.setAllowedHeaders(List.of("*"));
                    corsConfig.setAllowCredentials(true);
                    return corsConfig;
                }))
                .csrf(csrf -> csrf.disable())
                .exceptionHandling(ex -> ex.authenticationEntryPoint(unauthorizedRequestHandler))
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/users/signup", "/api/v1/users/signup/**").permitAll()
                        .requestMatchers("/api/v1/users/signin", "/api/v1/users/signin/**").permitAll()
                        .requestMatchers("/api/v1/webhooks/", "/api/v1/webhooks/**").permitAll()
                        .requestMatchers("/api/v1/telemetry/manual-ingest", "/api/v1/telemetry/manual-ingest/**").permitAll()
                        .requestMatchers(
                                "/api/v1/users/available-roles",
                                "/v3/api-docs/**",
                                "/signup",
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/swagger-resources/**",
                                "/webjars/**")
                        .permitAll()
                        .anyRequest().authenticated())
                // 🔑 Autenticación personalizada
                .authenticationProvider(authenticationProvider())
                // 🧱 Filtro JWT antes del UsernamePasswordAuthenticationFilter
                .addFilterBefore(authorizationRequestFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
