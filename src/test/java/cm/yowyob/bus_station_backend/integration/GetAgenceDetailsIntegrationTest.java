package cm.yowyob.bus_station_backend.integration;

import cm.yowyob.bus_station_backend.BaseIntegrationTest;
import cm.yowyob.bus_station_backend.application.dto.agence.AgenceVoyageResponseDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.r2dbc.core.DatabaseClient;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Tests d'intégration - Get Agence Details")
class GetAgenceDetailsIntegrationTest extends BaseIntegrationTest {

    @Test
    @DisplayName("Devrait retourner les détails complets d'une agence par son ID")
    void shouldGetAgenceDetailsSuccessfully() {
        // Given
        UUID organizationId = createTestOrganization();
        UUID agencyId = UUID.randomUUID();
        String longName = "Agence de Voyage Interurbain";
        String shortName = "AVI";
        String location = "Yaoundé, Mvan";
        String description = "Une agence de voyage de luxe";
        
        insertAgenceInDb(agencyId, organizationId, testAdminId, longName, shortName, location, description);

        // When & Then
        webTestClient.get()
                .uri("/agence/{id}", agencyId)
                .header("Authorization", "Bearer " + adminToken)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(AgenceVoyageResponseDTO.class)
                .value(response -> {
                    assertThat(response).isNotNull();
                    assertThat(response.getId()).isEqualTo(agencyId);
                    assertThat(response.getOrganisationId()).isEqualTo(organizationId);
                    assertThat(response.getUserId()).isEqualTo(testAdminId);
                    assertThat(response.getLongName()).isEqualTo(longName);
                    assertThat(response.getShortName()).isEqualTo(shortName);
                    assertThat(response.getLocation()).isEqualTo(location);
                    assertThat(response.getDescription()).isEqualTo(description);
                    // On vérifie que les champs par défaut du mapper sont bien là
                    assertThat(response.getLogoUrl()).isEqualTo("/placeholder.svg");
                    assertThat(response.getRating()).isEqualTo(0.0);
                    assertThat(response.getContact()).isNotNull();
                });
    }

    @Test
    @DisplayName("Devrait retourner 404 quand l'agence n'existe pas")
    void shouldReturn404WhenAgenceNotFound() {
        // Given
        UUID nonExistentId = UUID.randomUUID();

        // When & Then
        webTestClient.get()
                .uri("/agence/{id}", nonExistentId)
                .header("Authorization", "Bearer " + adminToken)
                .exchange()
                .expectStatus().isNotFound();
    }

    // ===== Méthodes utilitaires locales =====

    private UUID createTestOrganization() {
        UUID orgId = UUID.randomUUID();
        databaseClient
                .sql("""
                            INSERT INTO organization (id, organization_id, long_name, short_name, status, is_active)
                            VALUES (:id, :orgId, :name, :shortName, :status, :active)
                        """)
                .bind("id", orgId)
                .bind("orgId", orgId)
                .bind("name", "Test Organization")
                .bind("shortName", "TO")
                .bind("status", "ACTIVE")
                .bind("active", true)
                .then()
                .block();
        return orgId;
    }

    private void insertAgenceInDb(UUID agencyId, UUID organizationId, UUID userId, String longName, String shortName, String location, String description) {
        databaseClient
                .sql("""
                            INSERT INTO agences_voyage
                            (agency_id, organisation_id, user_id, name, short_name, location, description, greeting_message)
                            VALUES (:agencyId, :orgId, :userId, :longName, :shortName, :location, :description, :greeting)
                        """)
                .bind("agencyId", agencyId)
                .bind("orgId", organizationId)
                .bind("userId", userId)
                .bind("longName", longName)
                .bind("shortName", shortName)
                .bind("location", location)
                .bind("description", description)
                .bind("greeting", "Bienvenue chez " + longName)
                .then()
                .block();
    }
}
