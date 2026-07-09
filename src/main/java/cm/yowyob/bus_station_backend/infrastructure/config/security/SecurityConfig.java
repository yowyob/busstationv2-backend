package cm.yowyob.bus_station_backend.infrastructure.config.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

        private final AuthenticationManager authenticationManager;
        private final SecurityContextRepository securityContextRepository;
        private final CorsConfigurationSource corsConfigurationSource;

        @Bean
        public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
                return http
                        .cors(cors -> cors.configurationSource(corsConfigurationSource))
                        .exceptionHandling(exceptionHandlingSpec -> exceptionHandlingSpec
                                .authenticationEntryPoint(jsonAuthenticationEntryPoint())
                                .accessDeniedHandler(jsonAccessDeniedHandler()))
                        .csrf(ServerHttpSecurity.CsrfSpec::disable)
                        .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                        .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                        .authenticationManager(authenticationManager)
                        .securityContextRepository(securityContextRepository)
                        .authorizeExchange(exchanges -> exchanges
                                .pathMatchers(HttpMethod.OPTIONS).permitAll()
                                .pathMatchers("/utilisateur/inscription", "/utilisateur/connexion",
                                        "/utilisateur/test")
                                .permitAll()
                                .pathMatchers("/auth/register", "/auth/login", "/auth/refresh",
                                        "/auth/forgot-password", "/auth/reset-password")
                                .permitAll()
                                .pathMatchers("/organizations/**").permitAll()
                                .pathMatchers(HttpMethod.GET, "/gare", "/gare/{gareId}",
                                        "/gare/{gareId}/agences", "/gare/{gareId}/voyages")
                                                .permitAll()
                                .pathMatchers("/agence/gare-routiere/*").permitAll()
                                .pathMatchers("/agence").permitAll()
                                .pathMatchers(HttpMethod.GET, "/agence/*/public").permitAll()
                                .pathMatchers(HttpMethod.GET, "/organizations/*").permitAll()
                                .pathMatchers(HttpMethod.GET, "/ligne-service/public/**").permitAll()
                                .pathMatchers(HttpMethod.GET, "/voyage", "/voyage/{id}",
                                        "/voyage/agence/**", "/voyage/gare/**",
                                        "/voyage/*/similaires", "/voyage/search")
                                                .permitAll()
                                .pathMatchers(HttpMethod.POST, "/paiement/confirmer", "/paiement/echec").permitAll()
                                // --- LOT 9 : politiques de gare lecture publique ---
                                .pathMatchers(HttpMethod.GET, "/politique-gare/gare/**").permitAll()
                                .pathMatchers("/ping").permitAll()
                                .pathMatchers("/swagger-ui.html",
                                        "/swagger-ui/**",
                                        "/v3/api-docs",
                                        "/v3/api-docs/**",
                                        "/webjars/**",
                                        "/actuator/**",
                                        "/favicon.ico")
                                                .permitAll()
                                .anyExchange().authenticated())
                        .build();
        }

        /**
         * Retourne un JSON propre pour les erreurs d'authentification (401)
         * au lieu de laisser remonter un 500.
         */
        private ServerAuthenticationEntryPoint jsonAuthenticationEntryPoint() {
                return (exchange, ex) -> {
                        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

                        String body = """
                                {"status":401,"error":"Unauthorized","message":"%s"}"""
                                .formatted(ex.getMessage() != null ? ex.getMessage()
                                        : "Authentification requise");

                        DataBuffer buffer = exchange.getResponse()
                                .bufferFactory()
                                .wrap(body.getBytes(StandardCharsets.UTF_8));
                        return exchange.getResponse().writeWith(Mono.just(buffer));
                };
        }

        /**
         * Retourne un JSON propre pour les erreurs d'accès refusé (403)
         */
        private ServerAccessDeniedHandler jsonAccessDeniedHandler() {
                return (exchange, denied) -> {
                        exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

                        String body = """
                                {"status":403,"error":"Forbidden","message":"%s"}"""
                                .formatted(denied.getMessage() != null ?
                                        denied.getMessage() : "Accès refusé");

                        DataBuffer buffer = exchange.getResponse()
                                .bufferFactory()
                                .wrap(body.getBytes(StandardCharsets.UTF_8));
                        return exchange.getResponse().writeWith(Mono.just(buffer));
                };
        }
}
