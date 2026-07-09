package cm.yowyob.bus_station_backend.integration;

import cm.yowyob.bus_station_backend.BaseIntegrationTest;
import cm.yowyob.bus_station_backend.integration.RestPageImpl;
import cm.yowyob.bus_station_backend.application.dto.agence.AgenceVoyageResponseDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Tests d'intégration - AgenceVoyage GetAll Custom")
class AgenceVoyageGetAllIntegrationTest extends BaseIntegrationTest {

    @Test
    @DisplayName("Devrait retourner toutes les agences au format personnalisé avec pagination")
    void shouldReturnAllAgencesCustomFormat() {
        // Given
        UUID orgId = createTestOrganization();
        createTestAgenceInDb(orgId, testAdminId, "Agence Alpha", "AA");
        createTestAgenceInDb(orgId, testAdminId, "Agence Beta", "AB");

        // When & Then
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/agence")
                        .queryParam("page", 0)
                        .queryParam("size", 10)
                        .build())
                .header("Authorization", "Bearer " + adminToken)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(new ParameterizedTypeReference<RestPageImpl<AgenceVoyageResponseDTO>>() {})
                .value(page -> {
                    assertThat(page).isNotNull();
                    assertThat(page.getTotalElements()).isGreaterThanOrEqualTo(2);
                    
                    List<AgenceVoyageResponseDTO> content = page.getContent();
                    assertThat(content).hasSizeGreaterThanOrEqualTo(2);
                    
                    // Vérifier le format JSON (structure attendue par l'UI)
                    AgenceVoyageResponseDTO first = content.get(0);
                    assertThat(first.getId()).isNotNull();
                    assertThat(first.getLongName()).isNotNull();
                    assertThat(first.getLogoUrl()).isEqualTo("/placeholder.svg");
                    assertThat(first.getContact()).isNotNull();
                    assertThat(first.getContact().getEmail()).isEmpty();
                    assertThat(first.getGareIds()).isNotNull();
                });
    }

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

    private void createTestAgenceInDb(UUID organizationId, UUID userId, String longName, String shortName) {
        databaseClient
                .sql("""
                            INSERT INTO agences_voyage
                            (agency_id, organisation_id, user_id, name, short_name, location)
                            VALUES (:agencyId, :orgId, :userId, :longName, :shortName, :location)
                        """)
                .bind("agencyId", UUID.randomUUID())
                .bind("orgId", organizationId)
                .bind("userId", userId)
                .bind("longName", longName)
                .bind("shortName", shortName)
                .bind("location", "Yaoundé")
                .then()
                .block();
    }
}
