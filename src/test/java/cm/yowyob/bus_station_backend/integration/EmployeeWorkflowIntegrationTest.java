package cm.yowyob.bus_station_backend.integration;

import cm.yowyob.bus_station_backend.BaseIntegrationTest;
import cm.yowyob.bus_station_backend.application.dto.user.EmployeResponseDTO;
import cm.yowyob.bus_station_backend.domain.enums.StatutEmploye;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Tests d'intégration - Employés")
class EmployeeWorkflowIntegrationTest extends BaseIntegrationTest {

    @Test
    @DisplayName("Devrait retourner la liste des employés avec poste, département et statut")
    void shouldGetEmployeesByAgenceIdWithAllDetails() {
        // Given
        UUID organizationId = createTestOrganization();
        UUID agenceId = UUID.randomUUID();
        String agenceName = "Agence Express";
        insertAgenceInDb(agenceId, organizationId, testAdminId, agenceName, "AE", "Yaoundé", "Description");

        UUID employeeUserId = UUID.randomUUID();
        String employeeEmail = "emp@test.com";
        insertUserInDb(employeeUserId, employeeEmail, "EMPLOYE");

        UUID employeId = UUID.randomUUID();
        String poste = "Responsable Logistique";
        String departement = "Opérations";
        StatutEmploye statut = StatutEmploye.ACTIF;

        insertEmployeInDb(employeId, agenceId, employeeUserId, poste, departement, statut);

        // When & Then
        webTestClient.get()
                .uri("/utilisateur/employes/{agenceId}", agenceId)
                .header("Authorization", "Bearer " + adminToken)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(EmployeResponseDTO.class)
                .value(employees -> {
                    assertThat(employees).isNotEmpty();
                    EmployeResponseDTO dto = employees.get(0);
                    assertThat(dto.getEmployeId()).isEqualTo(employeId);
                    assertThat(dto.getUserId()).isEqualTo(employeeUserId);
                    assertThat(dto.getEmail()).isEqualTo(employeeEmail);
                    assertThat(dto.getPoste()).isEqualTo(poste);
                    assertThat(dto.getDepartement()).isEqualTo(departement);
                    assertThat(dto.getStatutEmploye()).isEqualTo(statut);
                    assertThat(dto.getNomAgence()).isEqualTo(agenceName);
                });
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
                .bind("greeting", "Bienvenue")
                .then()
                .block();
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

    private void insertEmployeInDb(UUID employeId, UUID agenceId, UUID userId, String poste, String departement, StatutEmploye statut) {
        databaseClient.sql("""
                INSERT INTO employes (id, agence_id, user_id, poste, departement, statut_employe, date_embauche)
                VALUES (:employeId, :agenceId, :userId, :poste, :departement, :statut, :dateEmbauche)
                """)
                .bind("employeId", employeId)
                .bind("agenceId", agenceId)
                .bind("userId", userId)
                .bind("poste", poste)
                .bind("departement", departement)
                .bind("statut", statut.name())
                .bind("dateEmbauche", LocalDateTime.now())
                .then()
                .block();
    }
}
