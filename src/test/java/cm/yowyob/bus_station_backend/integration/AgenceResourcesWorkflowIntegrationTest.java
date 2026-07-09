package cm.yowyob.bus_station_backend.integration;

import cm.yowyob.bus_station_backend.BaseIntegrationTest;
import cm.yowyob.bus_station_backend.application.dto.vehicule.VehiculeDTO;
import cm.yowyob.bus_station_backend.application.dto.user.ChauffeurRequestDTO;
import cm.yowyob.bus_station_backend.application.dto.classVoyage.ClassVoyageDTO;
import org.junit.jupiter.api.*;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThat;

@DisplayName("Tests d'intégration - Workflow Ressources Agence")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AgenceResourcesWorkflowIntegrationTest extends BaseIntegrationTest {

    private UUID agenceId;

    @BeforeEach
    void setUp() {
        // Préparer une agence pour les tests
        UUID organizationId = createTestOrganization();
        UUID gareId = createTestGare();
        agenceId = createTestAgenceInDb(organizationId, testAdminId, gareId);
    }

    @Test
    @Order(1)
    @DisplayName("CRUD Véhicule")
    void vehiculeWorkflow() {
        VehiculeDTO vehiculeDTO = new VehiculeDTO();
        vehiculeDTO.setNom("Bus de test");
        vehiculeDTO.setModele("Model X");
        vehiculeDTO.setNbrPlaces(30);
        vehiculeDTO.setPlaqueMatricule("TEST-" + UUID.randomUUID().toString().substring(0, 5));
        vehiculeDTO.setIdAgenceVoyage(agenceId);

        // CREATE
        VehiculeDTO created = webTestClient.post()
                .uri("/vehicule")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(vehiculeDTO)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(VehiculeDTO.class)
                .returnResult()
                .getResponseBody();

        assertThat(created).isNotNull();
        assertThat(created.getIdVehicule()).isNotNull();
        UUID vehiculeId = created.getIdVehicule();

        // READ
        webTestClient.get()
                .uri("/vehicule/agence/{agenceId}", agenceId)
                .header("Authorization", "Bearer " + adminToken)
                .exchange()
                .expectStatus().isOk();

        // UPDATE
        created.setNom("Bus Mis à jour");
        webTestClient.put()
                .uri("/vehicule/{id}", vehiculeId)
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(created)
                .exchange()
                .expectStatus().isOk()
                .expectBody(VehiculeDTO.class)
                .value(updated -> {
                    assertThat(updated.getNom()).isEqualTo("Bus Mis à jour");
                });

        // DELETE
        webTestClient.delete()
                .uri("/vehicule/{id}", vehiculeId)
                .header("Authorization", "Bearer " + adminToken)
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    @Order(2)
    @DisplayName("CRUD Chauffeur")
    void chauffeurWorkflow() {
        ChauffeurRequestDTO chauffeurDTO = new ChauffeurRequestDTO();
        chauffeurDTO.setLast_name("Chauffeur Test");
        chauffeurDTO.setFirst_name("Test");
        chauffeurDTO.setAgenceVoyageId(agenceId);
        chauffeurDTO.setEmail("chauffeur" + UUID.randomUUID() + "@test.com");
        chauffeurDTO.setPassword("Pass1234");
        chauffeurDTO.setGender(cm.yowyob.bus_station_backend.domain.enums.Gender.MALE);
        chauffeurDTO.setRole(java.util.List.of(cm.yowyob.bus_station_backend.domain.enums.RoleType.EMPLOYE));
        chauffeurDTO.setUsername("chauffeur" + UUID.randomUUID().toString().substring(0, 5));

        // CREATE
        var created = webTestClient.post()
                .uri("/utilisateur/chauffeur")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(chauffeurDTO)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(cm.yowyob.bus_station_backend.application.dto.user.UserResponseCreatedDTO.class)
                .value(response -> {
                    assertThat(response.getEmail()).isEqualTo(chauffeurDTO.getEmail());
                    assertThat(response.getUsername()).isEqualTo(chauffeurDTO.getUsername());
                })
                .returnResult()
                .getResponseBody();

        assertThat(created).isNotNull();
        }

        @Test
        @Order(3)
        @DisplayName("CRUD Classe Voyage")
        void classVoyageWorkflow() {
            ClassVoyageDTO classDTO = new ClassVoyageDTO();
            classDTO.setNom("VIP");
            classDTO.setPrix(10000.0);
            classDTO.setIdAgenceVoyage(agenceId);

            // CREATE
            var created = webTestClient.post()
                    .uri("/class-voyage")
                    .header("Authorization", "Bearer " + adminToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(classDTO)
                    .exchange()
                    .expectStatus().isCreated()
                    .expectBody(ClassVoyageDTO.class)
                    .value(response -> {
                        assertThat(response.getNom()).isEqualTo("VIP");
                        assertThat(response.getPrix()).isEqualTo(10000.0);
                    })
                    .returnResult()
                    .getResponseBody();

            // READ
            webTestClient.get()
                    .uri("/class-voyage/agence/{agenceId}", agenceId)
                    .header("Authorization", "Bearer " + adminToken)
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody(new ParameterizedTypeReference<List<ClassVoyageDTO>>() {})
                    .value(list -> {
                        assertThat(list).hasSize(1);
                        assertThat(list.get(0).getNom()).isEqualTo("VIP");
                    });
        }
    private UUID createTestOrganization() {
        UUID orgId = UUID.randomUUID();
        databaseClient
                .sql("INSERT INTO organization (id, organization_id, status) VALUES (:id, :orgId, 'ACTIVE')")
                .bind("id", orgId)
                .bind("orgId", orgId)
                .then()
                .block();
        return orgId;
    }

    private UUID createTestGare() {
        UUID gareId = UUID.randomUUID();
        databaseClient
                .sql("INSERT INTO gare_routiere (id_gare_routiere, nom_gare_routiere, manager_id) VALUES (:id, 'Gare Test', :managerId)")
                .bind("id", gareId)
                .bind("managerId", testAdminId)
                .then()
                .block();
        return gareId;
    }

    private UUID createTestAgenceInDb(UUID organizationId, UUID userId, UUID gareId) {
        UUID agencyId = UUID.randomUUID();
        databaseClient
                .sql("""
            INSERT INTO agences_voyage
            (agency_id, organisation_id, user_id, name, location, gare_routiere_id)
            VALUES (:agencyId, :orgId, :userId, 'Agence Test', 'Yaoundé', :gareId)
        """)
                .bind("agencyId", agencyId)
                .bind("orgId", organizationId)
                .bind("userId", userId)
                .bind("gareId", gareId)
                .then()
                .block();
        return agencyId;
    }
}
