package cm.yowyob.bus_station_backend.integration;

import cm.yowyob.bus_station_backend.BaseIntegrationTest;
import cm.yowyob.bus_station_backend.application.dto.user.*;
import cm.yowyob.bus_station_backend.domain.enums.Gender;
import cm.yowyob.bus_station_backend.domain.enums.RoleType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Tests d'intégration - Mise à jour Utilisateur (Employé/Chauffeur)")
class UserUpdateIntegrationTest extends BaseIntegrationTest {

    @Test
    @DisplayName("Devrait créer et mettre à jour un employé avec succès")
    void shouldCreateAndUpdateEmployeSuccessfully() {
        // Given
        UUID organizationId = createTestOrganization();
        UUID agenceId = createTestAgenceInDb(organizationId, testAdminId);

        EmployeRequestDTO createDto = new EmployeRequestDTO();
        createDto.setFirst_name("Employe");
        createDto.setLast_name("Test");
        createDto.setEmail("employe.test@example.com");
        createDto.setUsername("employetest");
        createDto.setPassword("Password123!");
        createDto.setPhone_number("+237600000001");
        createDto.setGender(Gender.MALE);
        createDto.setRole(List.of(RoleType.EMPLOYE));
        createDto.setAgenceVoyageId(agenceId);
        createDto.setPoste("Guichetier");
        createDto.setDepartement("Ventes");

        // 1. Création
        UserResponseCreatedDTO created = webTestClient.post()
                .uri("/utilisateur/employe")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(createDto)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(UserResponseCreatedDTO.class)
                .returnResult()
                .getResponseBody();

        assertThat(created).isNotNull();
        UUID userId = UUID.fromString(created.getId());

        // Récupérer l'ID de l'employé depuis la base
        UUID employeId = databaseClient.sql("SELECT id FROM employes WHERE user_id = :userId")
                .bind("userId", userId)
                .map(row -> row.get("id", UUID.class))
                .one()
                .block();

        assertThat(employeId).isNotNull();

        // 2. Mise à jour
        EmployeRequestDTO updateDto = new EmployeRequestDTO();
        updateDto.setFirst_name("Employe-Modifie");
        updateDto.setPoste("Chef de Gare");
        updateDto.setAgenceVoyageId(agenceId); // Requis par @Valid

        webTestClient.put()
                .uri("/utilisateur/employe/{id}", employeId)
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updateDto)
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserResponseCreatedDTO.class)
                .value(response -> {
                    assertThat(response.getFirst_name()).isEqualTo("Employe-Modifie");
                });

        // 3. Vérifier en base
        databaseClient.sql("SELECT nom, prenom FROM users WHERE user_id = :userId")
                .bind("userId", userId)
                .map(row -> {
                    assertThat(row.get("prenom", String.class)).isEqualTo("Employe-Modifie");
                    return true;
                })
                .one()
                .block();

        databaseClient.sql("SELECT poste FROM employes WHERE id = :employeId")
                .bind("employeId", employeId)
                .map(row -> {
                    assertThat(row.get("poste", String.class)).isEqualTo("Chef de Gare");
                    return true;
                })
                .one()
                .block();
    }

    @Test
    @DisplayName("Devrait créer et mettre à jour un chauffeur avec succès")
    void shouldCreateAndUpdateChauffeurSuccessfully() {
        // Given
        UUID organizationId = createTestOrganization();
        UUID agenceId = createTestAgenceInDb(organizationId, testAdminId);

        ChauffeurRequestDTO createDto = new ChauffeurRequestDTO();
        createDto.setFirst_name("Chauffeur");
        createDto.setLast_name("Test");
        createDto.setEmail("chauffeur.test@example.com");
        createDto.setUsername("chauffeurtest");
        createDto.setPassword("Password123!");
        createDto.setPhone_number("+237600000002");
        createDto.setGender(Gender.MALE);
        createDto.setRole(List.of(RoleType.EMPLOYE));
        createDto.setAgenceVoyageId(agenceId);

        // 1. Création
        UserResponseCreatedDTO created = webTestClient.post()
                .uri("/utilisateur/chauffeur")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(createDto)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(UserResponseCreatedDTO.class)
                .returnResult()
                .getResponseBody();

        assertThat(created).isNotNull();
        UUID userId = UUID.fromString(created.getId());

        // Récupérer l'ID du chauffeur depuis la base
        UUID chauffeurId = databaseClient.sql("SELECT id FROM chauffeurs WHERE user_id = :userId")
                .bind("userId", userId)
                .map(row -> row.get("id", UUID.class))
                .one()
                .block();

        assertThat(chauffeurId).isNotNull();

        // 2. Mise à jour
        ChauffeurRequestDTO updateDto = new ChauffeurRequestDTO();
        updateDto.setLast_name("Chauffeur-Modifie");
        updateDto.setAgenceVoyageId(agenceId);

        webTestClient.put()
                .uri("/utilisateur/chauffeur/{id}", chauffeurId)
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updateDto)
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserResponseCreatedDTO.class)
                .value(response -> {
                    assertThat(response.getLast_name()).isEqualTo("Chauffeur-Modifie");
                });

        // 3. Vérifier en base
        databaseClient.sql("SELECT nom FROM users WHERE user_id = :userId")
                .bind("userId", userId)
                .map(row -> {
                    assertThat(row.get("nom", String.class)).isEqualTo("Chauffeur-Modifie");
                    return true;
                })
                .one()
                .block();
    }

    // ===== Méthodes utilitaires =====

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
}
