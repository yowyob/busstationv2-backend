package cm.yowyob.bus_station_backend;

import cm.yowyob.bus_station_backend.infrastructure.config.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import org.springframework.http.HttpHeaders;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("test")
public abstract class BaseIntegrationTest {

    @Autowired
    protected WebTestClient webTestClient;

    @Autowired
    protected DatabaseClient databaseClient;

    @Autowired
    protected JwtService jwtService;

    @MockBean
    protected KafkaTemplate<String, Object> kafkaTemplate;

    @MockBean
    protected ReactiveStringRedisTemplate reactiveStringRedisTemplate;

    @MockBean
    protected ReactiveValueOperations<String, String> reactiveValueOperations;

    @MockBean
    protected cm.yowyob.bus_station_backend.application.port.out.PaymentPort paymentPort;

    protected String adminToken;
    protected String userToken;
    protected String bsmToken;
    protected UUID testUserId;
    protected UUID testAdminId;
    protected UUID testBsmId;

    @BeforeEach
    protected void setUpBase() {
        when(reactiveStringRedisTemplate.opsForValue()).thenReturn(reactiveValueOperations);
        when(reactiveStringRedisTemplate.hasKey(anyString())).thenReturn(Mono.just(false));
        when(reactiveValueOperations.set(anyString(), anyString(), any())).thenReturn(Mono.just(true));
        
        // Mocking TokenStoreService.consumePasswordResetToken: get then delete
        // Le token est passé comme argument à get(), on doit retourner la valeur stockée (le username)
        when(reactiveValueOperations.get(anyString())).thenAnswer(invocation -> {
            String key = invocation.getArgument(0);
            if (key.startsWith("auth:pwd-reset:")) {
                return Mono.just("testuser");
            }
            return Mono.empty();
        });
        when(reactiveStringRedisTemplate.delete(anyString())).thenReturn(Mono.just(1L));
        
        cleanDatabase();
        setupTestUsers();
    }

    protected void cleanDatabase() {
        List<String> tables = List.of(
                "politique_et_taxes",
                "affiliation_agence_voyage",
                "coupons",
                "baggages",
                "passagers",
                "historiques",
                "taux_periode",
                "lignes_voyage",
                "reservations",
                "soldes_indemnisation",
                "politiques_annulation",
                "class_voyage",
                "chauffeurs",
                "employes",
                "vehicules",
                "agences_voyage",
                "gare_routiere",
                "users",
                "voyages",
                "organization",
                "coordonnee"
        );

        databaseClient.sql("SET REFERENTIAL_INTEGRITY FALSE").then().block();
        Flux.fromIterable(tables)
                .flatMap(table -> databaseClient.sql("DELETE FROM " + table).then())
                .blockLast();
        databaseClient.sql("SET REFERENTIAL_INTEGRITY TRUE").then().block();
    }

    protected void setupTestUsers() {
        testUserId = UUID.randomUUID();
        testAdminId = UUID.randomUUID();
        testBsmId = UUID.randomUUID();

        insertUserInDb(testUserId, "user@test.com", "USAGER");
        insertUserInDb(testAdminId, "admin@test.com", "ADMIN,AGENCE_VOYAGE");
        insertUserInDb(testBsmId, "bsm@test.com", "BUS_STATION_MANAGER");

        userToken = jwtService.generateToken(
                testUserId.toString(),
                Map.of("roles", List.of("USAGER"), "userId", testUserId));

        adminToken = jwtService.generateToken(
                testAdminId.toString(),
                Map.of("roles", List.of("ADMIN", "AGENCE_VOYAGE"), "userId", testAdminId));

        bsmToken = jwtService.generateToken(
                testBsmId.toString(),
                Map.of("roles", List.of("BUS_STATION_MANAGER"), "userId", testBsmId));
    }

    private void insertUserInDb(UUID id, String email, String role) {
        databaseClient.sql("""
                INSERT INTO users (user_id, nom, prenom, email, username, password, roles)
                VALUES (:id, 'Test', 'User', :email, :username, 'pass', :role)
                """)
                .bind("id", id)
                .bind("email", email)
                .bind("username", id.toString())
                .bind("role", role)
                .then()
                .block();
    }

    /**
     * Helper pour WebTestClient avec token
     */
    protected WebTestClient authenticatedClient(String token) {
        return webTestClient.mutate()
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .build();
    }
}
