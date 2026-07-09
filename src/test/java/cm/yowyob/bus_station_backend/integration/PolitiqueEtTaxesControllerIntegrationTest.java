package cm.yowyob.bus_station_backend.integration;

import cm.yowyob.bus_station_backend.BaseIntegrationTest;
import cm.yowyob.bus_station_backend.application.dto.politique.PolitiqueEtTaxesRequestDTO;
import cm.yowyob.bus_station_backend.domain.enums.PolitiqueOuTaxe;
import cm.yowyob.bus_station_backend.infrastructure.outbound.external.HttpMediaServiceAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

class PolitiqueEtTaxesControllerIntegrationTest extends BaseIntegrationTest {

        @Autowired
        private WebTestClient webTestClient;

        @MockBean
        private HttpMediaServiceAdapter httpMediaServiceAdapter;

        private UUID savedGareRoutiereId;
        private UUID savedPolitiqueEtTaxesId;

        @BeforeEach
        void setUp() {
                Mockito.when(httpMediaServiceAdapter.uploadFile(any(), anyString()))
                                .thenReturn(Mono.just("http://mock-media-service.com/file.pdf"));

                databaseClient.sql("DELETE FROM politique_et_taxes").then().block();
                databaseClient.sql("DELETE FROM gare_routiere").then().block();

                savedGareRoutiereId = UUID.randomUUID();
                databaseClient.sql(
                                "INSERT INTO gare_routiere (id_gare_routiere, nom_gare_routiere, ville, services, manager_id) VALUES (:id, :nom, :ville, :services, :managerId)")
                                .bind("id", savedGareRoutiereId)
                                .bind("nom", "Gare de Test")
                                .bind("ville", "TestVille")
                                .bind("services", "WIFI")
                                .bind("managerId", testAdminId)
                                .then()
                                .block();

                savedPolitiqueEtTaxesId = UUID.randomUUID();
                databaseClient.sql(
                                "INSERT INTO politique_et_taxes (id_politique, gare_routiere_id, nom_politique, description, taux_taxe, montant_fixe, date_effet, document_url, type) VALUES (:id, :gareRoutiereId, :nom, :description, :taux, :montant, :date, :docUrl, :type)")
                                .bind("id", savedPolitiqueEtTaxesId)
                                .bind("gareRoutiereId", savedGareRoutiereId)
                                .bind("nom", "Politique Existante")
                                .bind("description", "Description de la politique existante")
                                .bind("taux", 0.05)
                                .bind("montant", 10.0)
                                .bind("date", LocalDate.now())
                                .bind("docUrl", "http://example.com/existing-doc.pdf")
                                .bind("type", PolitiqueOuTaxe.POLITIQUE.name())
                                .then()
                                .block();
        }

        @Test
        void createPolitique_shouldReturnCreatedPolitique() {
                PolitiqueEtTaxesRequestDTO requestDTO = PolitiqueEtTaxesRequestDTO.builder()
                                .gareRoutiereId(savedGareRoutiereId)
                                .nomPolitique("Nouvelle Politique")
                                .description("Description de la nouvelle politique")
                                .tauxTaxe(0.05)
                                .montantFixe(10.0)
                                .dateEffet(LocalDate.now())
                                .type(PolitiqueOuTaxe.TAXE)
                                .build();

                MultipartBodyBuilder builder = new MultipartBodyBuilder();
                builder.asyncPart("politique", Mono.just(requestDTO), PolitiqueEtTaxesRequestDTO.class)
                                .contentType(MediaType.APPLICATION_JSON);
                builder.part("file", new ByteArrayResource("test content".getBytes()) {
                        @Override
                        public String getFilename() {
                                return "test.pdf";
                        }
                }).contentType(MediaType.APPLICATION_PDF);

                authenticatedClient(adminToken).post().uri("/politique-et-taxes/add")
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .body(BodyInserters.fromMultipartData(builder.build()))
                                .exchange()
                                .expectStatus().isCreated()
                                .expectBody()
                                .jsonPath("$.nomPolitique").isEqualTo("Nouvelle Politique")
                                .jsonPath("$.documentUrl").isNotEmpty();
        }

        @Test
        void getPolitiqueById_shouldReturnPolitique() {
                authenticatedClient(adminToken).get().uri("/politique-et-taxes/{id}", savedPolitiqueEtTaxesId)
                                .exchange()
                                .expectStatus().isOk()
                                .expectBody()
                                .jsonPath("$.idPolitique").isEqualTo(savedPolitiqueEtTaxesId.toString())
                                .jsonPath("$.nomPolitique").isEqualTo("Politique Existante");
        }

        @Test
        void getAllPolitiquesByGareRoutiere_shouldReturnPolitiques() {
                authenticatedClient(adminToken).get()
                                .uri("/politique-et-taxes/gare-routiere/{gareRoutiereId}", savedGareRoutiereId)
                                .exchange()
                                .expectStatus().isOk()
                                .expectBody()
                                .jsonPath("$").isArray()
                                .jsonPath("$.length()").isEqualTo(1)
                                .jsonPath("$[0].idPolitique").isEqualTo(savedPolitiqueEtTaxesId.toString());
        }

        @Test
        void updatePolitique_shouldReturnUpdatedPolitique() {
                PolitiqueEtTaxesRequestDTO requestDTO = PolitiqueEtTaxesRequestDTO.builder()
                                .gareRoutiereId(savedGareRoutiereId)
                                .nomPolitique("Politique Updated")
                                .description("Description Updated")
                                .tauxTaxe(0.10)
                                .montantFixe(20.0)
                                .dateEffet(LocalDate.now())
                                .type(PolitiqueOuTaxe.POLITIQUE)
                                .build();

                MultipartBodyBuilder builder = new MultipartBodyBuilder();
                builder.asyncPart("politique", Mono.just(requestDTO), PolitiqueEtTaxesRequestDTO.class)
                                .contentType(MediaType.APPLICATION_JSON);
                builder.part("file", new ByteArrayResource("updated content".getBytes()) {
                        @Override
                        public String getFilename() {
                                return "updated.txt";
                        }
                }).contentType(MediaType.TEXT_PLAIN);

                authenticatedClient(adminToken).put().uri("/politique-et-taxes/{id}", savedPolitiqueEtTaxesId)
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .body(BodyInserters.fromMultipartData(builder.build()))
                                .exchange()
                                .expectStatus().isOk()
                                .expectBody()
                                .jsonPath("$.nomPolitique").isEqualTo("Politique Updated")
                                .jsonPath("$.documentUrl").isNotEmpty();
        }

        @Test
        void deletePolitique_shouldReturnNoContent() {
                authenticatedClient(adminToken).delete().uri("/politique-et-taxes/{id}", savedPolitiqueEtTaxesId)
                                .exchange()
                                .expectStatus().isNoContent();

                Mono<Long> count = databaseClient
                                .sql("SELECT COUNT(*) FROM politique_et_taxes WHERE id_politique = :id")
                                .bind("id", savedPolitiqueEtTaxesId)
                                .map(row -> row.get(0, Long.class))
                                .one();

                assertThat(count.block()).isEqualTo(0L);
        }
}
