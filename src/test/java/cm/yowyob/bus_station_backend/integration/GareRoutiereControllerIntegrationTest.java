package cm.yowyob.bus_station_backend.integration;

import cm.yowyob.bus_station_backend.BaseIntegrationTest;
import cm.yowyob.bus_station_backend.application.dto.gareRoutiere.GareRoutiereDTO;
import cm.yowyob.bus_station_backend.application.dto.gareRoutiere.GareRoutierePreviewDTO;
import cm.yowyob.bus_station_backend.application.dto.gareRoutiere.GareRoutiereRequestDTO;
import cm.yowyob.bus_station_backend.domain.enums.ServicesGareRoutiere;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class GareRoutiereControllerIntegrationTest extends BaseIntegrationTest {

    @BeforeEach
    void setUp() {
        databaseClient.sql(
                "INSERT INTO gare_routiere (id_gare_routiere, nom_gare_routiere, ville, services, manager_id) VALUES (:id, :nom, :ville, :services, :managerId)")
                .bind("id", UUID.randomUUID())
                .bind("nom", "Gare de Mvan")
                .bind("ville", "Yaounde")
                .bind("services", "WIFI,PARKING")
                .bind("managerId", testAdminId)
                .then()
                .block();

        databaseClient.sql(
                "INSERT INTO gare_routiere (id_gare_routiere, nom_gare_routiere, ville, services, manager_id) VALUES (:id, :nom, :ville, :services, :managerId)")
                .bind("id", UUID.randomUUID())
                .bind("nom", "Gare de Douala")
                .bind("ville", "Douala")
                .bind("services", "RESTAURATION,TOILETTES")
                .bind("managerId", testAdminId)
                .then()
                .block();
    }

    @Test
    void shouldCreateGareRoutiereSuccessfully() {

        GareRoutiereRequestDTO requestDTO = new GareRoutiereRequestDTO();
        requestDTO.setNomGareRoutiere("Gare de Bafoussam");
        requestDTO.setVille("Bafoussam");
        requestDTO.setQuartier("Kouogouo");
        requestDTO.setManagerId(testAdminId);
        requestDTO.setNomPresident("President Name");
        requestDTO.setServices(List.of(ServicesGareRoutiere.WIFI, ServicesGareRoutiere.PARKING));

        authenticatedClient(adminToken).post()
                .uri("/gare")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestDTO)
                .exchange()
                .expectStatus().isCreated();

        // verify in database
        verifyGareRoutiereExistsInDatabase(requestDTO.getNomGareRoutiere());
        }

        @Test
    void shouldGetAllGaresRoutieres() {
        authenticatedClient(adminToken).get()
                .uri("/gare")
                .exchange()
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<RestPageImpl<GareRoutierePreviewDTO>>() {
                })
                .value(page -> {
                    assertThat(page.getContent()).hasSize(2);
                });
    }

    @Test
    void shouldGetGaresRoutieresBySearchTerm() {
        authenticatedClient(adminToken).get()
                .uri("/gare?searchTerm=Yaounde")
                .exchange()
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<RestPageImpl<GareRoutierePreviewDTO>>() {
                })
                .value(page -> {
                    assertThat(page.getContent()).hasSize(1);
                    assertThat(page.getContent().get(0).getVille()).isEqualTo("Yaounde");
                });
    }

    @Test
    void shouldGetGaresRoutieresByServices() {
        authenticatedClient(adminToken).get()
                .uri("/gare?services=WIFI,PARKING")
                .exchange()
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<RestPageImpl<GareRoutierePreviewDTO>>() {
                })
                .value(page -> {
                    assertThat(page.getContent()).hasSize(1);
                    assertThat(page.getContent().get(0).getServices()).contains(ServicesGareRoutiere.WIFI,
                            ServicesGareRoutiere.PARKING);
                });
    }

    private void verifyGareRoutiereExistsInDatabase(String name) {
        Long count = databaseClient
                .sql("SELECT COUNT(*) FROM gare_routiere WHERE nom_gare_routiere = :name")
                .bind("name", name)
                .map(row -> row.get(0, Long.class))
                .one()
                .block();

        assertThat(count).isEqualTo(1L);
    }
}
