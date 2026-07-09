package cm.yowyob.bus_station_backend.integration;

import cm.yowyob.bus_station_backend.BaseIntegrationTest;
import cm.yowyob.bus_station_backend.application.dto.agence.AgenceVoyageDTO;
import cm.yowyob.bus_station_backend.application.dto.agence.AgenceVoyageResponseDTO;
import cm.yowyob.bus_station_backend.helper.TestDataBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Tests d'intégration - AgenceVoyageController")
class AgenceVoyageControllerIntegrationTest extends BaseIntegrationTest {

    @Test
    @DisplayName("Devrait créer une agence avec succès")
    void shouldCreateAgenceSuccessfully() {
        // Given
        UUID organizationId = createTestOrganization();
        UUID gareId = createTestGare();
        AgenceVoyageDTO agenceDTO = TestDataBuilder.createTestAgence(organizationId, testAdminId, gareId);

        // When & Then
        webTestClient.post()
                .uri("/agence")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(agenceDTO)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(cm.yowyob.bus_station_backend.application.dto.agence.AgenceVoyageResponseDTO.class)
                .value(response -> {
                    assertThat(response).isNotNull();
                    assertThat(response.getLongName()).isEqualTo(agenceDTO.getLong_name());
                    assertThat(response.getShortName()).isEqualTo(agenceDTO.getShort_name());
                    assertThat(response.getLocation()).isEqualTo(agenceDTO.getLocation());
                });

        // Vérifier en base de données
        verifyAgenceExistsInDatabase(agenceDTO.getShort_name());
    }

    @Test
    @DisplayName("Devrait retourner 404 si l'organisation n'existe pas")
    void shouldReturn404WhenOrganizationNotFound() {
        // Given
        UUID nonExistentOrgId = UUID.randomUUID();
        UUID gareId = createTestGare();
        AgenceVoyageDTO agenceDTO = TestDataBuilder.createTestAgence(nonExistentOrgId, testAdminId, gareId);

        // When & Then
        webTestClient.post()
                .uri("/agence")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(agenceDTO)
                .header("Authorization", "Bearer " + adminToken)
                .exchange()
                .expectStatus().is4xxClientError();
    }

    @Test
    @DisplayName("Devrait mettre à jour une agence existante")
    void shouldUpdateExistingAgence() {
        // Given - Créer d'abord une agence
        UUID organizationId = createTestOrganization();
        UUID agenceId = createTestAgenceInDb(organizationId, testAdminId);

        AgenceVoyageDTO updateDTO = new AgenceVoyageDTO();
        updateDTO.setLong_name("Nouveau nom");
        updateDTO.setLocation("Douala");

        // When & Then
        webTestClient.patch()
                .uri("/agence/{id}", agenceId)
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updateDTO)
                .exchange()
                .expectStatus().isOk()
                .expectBody(AgenceVoyageDTO.class)
                .value(response -> {
                    assertThat(response.getLong_name()).isEqualTo("Nouveau nom");
                    assertThat(response.getLocation()).isEqualTo("Douala");
                });
    }

    @Test
    @DisplayName("Devrait refuser la mise à jour par un utilisateur non autorisé")
    void shouldDenyUpdateByUnauthorizedUser() {
        // Given
        UUID organizationId = createTestOrganization();
        UUID agenceId = createTestAgenceInDb(organizationId, testAdminId);

        AgenceVoyageDTO updateDTO = new AgenceVoyageDTO();
        updateDTO.setLong_name("Tentative de hack");

        // When & Then
        webTestClient.patch()
                .uri("/agence/{id}", agenceId)
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updateDTO)
                .exchange()
                .expectStatus().is4xxClientError();
    }

    @Test
    @DisplayName("Devrait supprimer une agence avec succès")
    void shouldDeleteAgenceSuccessfully() {
        // Given
        UUID organizationId = createTestOrganization();
        UUID agenceId = createTestAgenceInDb(organizationId, testAdminId);

        // When & Then
        webTestClient.delete()
                .uri("/agence/{id}", agenceId)
                .header("Authorization", "Bearer " + adminToken)
                .exchange()
                .expectStatus().isNoContent();

        // Vérifier que l'agence n'existe plus
        verifyAgenceNotExistsInDatabase(agenceId);
    }

    @Test
    @DisplayName("Devrait récupérer une agence par l'ID du chef d'agence")
    void shouldGetAgenceByChefAgenceId() {
        // Given
        UUID organizationId = createTestOrganization();
        createTestAgenceInDb(organizationId, testAdminId);

        // When & Then
        webTestClient.get()
                .uri("/agence/chef-agence/{id}", testAdminId)
                .header("Authorization", "Bearer " + adminToken)
                .exchange()
                .expectStatus().isOk()
                .expectBody(AgenceVoyageResponseDTO.class)
                .value(response -> {
                    assertThat(response).isNotNull();
                    assertThat(response.getUserId()).isEqualTo(testAdminId);
                });
    }

    @Test
    @DisplayName("Devrait retourner 401 sans authentification")
    void shouldReturn401WithoutAuthentication() {
        // When & Then
        webTestClient.get()
                .uri("/agence/chef-agence/{id}", UUID.randomUUID())
                .exchange()
                .expectStatus().isUnauthorized();
    }

    // ===== Méthodes utilitaires =====

    private UUID createTestGare() {
        UUID gareId = UUID.randomUUID();
        databaseClient
                .sql("""
                            INSERT INTO gare_routiere 
                            (id_gare_routiere, nom_gare_routiere, ville, quartier, nom_president, manager_id)
                            VALUES (:id, :nom, :ville, :quartier, :president, :managerId)
                        """)
                .bind("id", gareId)
                .bind("nom", "Gare de Yaoundé")
                .bind("ville", "Yaoundé")
                .bind("quartier", "Mvan")
                .bind("president", "Président Test")
                .bind("managerId", testAdminId)
                .then()
                .block();
        return gareId;
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

    private UUID createTestAgenceInDb(UUID organizationId, UUID userId) {
        UUID agencyId = UUID.randomUUID();
        databaseClient
                .sql("""
                            INSERT INTO agences_voyage
                            (agency_id, organisation_id, user_id, name, short_name, location)
                            VALUES (:agencyId, :orgId, :userId, :longName, :shortName, :location)
                        """)
                .bind("agencyId", agencyId)
                .bind("orgId", organizationId)
                .bind("userId", userId)
                .bind("longName", "Agence Test")
                .bind("shortName", "AT-" + UUID.randomUUID().toString().substring(0, 5))
                .bind("location", "Yaoundé")
                .then()
                .block();
        return agencyId;
    }

    private void verifyAgenceExistsInDatabase(String shortName) {
        Long count = databaseClient
                .sql("SELECT COUNT(*) FROM agences_voyage WHERE short_name = :shortName")
                .bind("shortName", shortName)
                .map(row -> row.get(0, Long.class))
                .one()
                .block();

        assertThat(count).isEqualTo(1L);
    }

    private void verifyAgenceNotExistsInDatabase(UUID agenceId) {
        Long count = databaseClient
                .sql("SELECT COUNT(*) FROM agences_voyage WHERE agency_id = :agenceId")
                .bind("agenceId", agenceId)
                .map(row -> row.get(0, Long.class))
                .one()
                .block();

        assertThat(count).isZero();
    }
}