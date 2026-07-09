package cm.yowyob.bus_station_backend.integration;

import cm.yowyob.bus_station_backend.BaseIntegrationTest;
import cm.yowyob.bus_station_backend.application.dto.planning.CreneauPlanningDTO;
import cm.yowyob.bus_station_backend.application.dto.planning.PlanningVoyageDTO;
import cm.yowyob.bus_station_backend.application.dto.voyage.VoyageBrouillonResponseDTO;
import cm.yowyob.bus_station_backend.application.dto.voyage.VoyageBrouillonUpdateDTO;
import cm.yowyob.bus_station_backend.application.dto.voyage.generation.GenerationUnitaireRequestDTO;
import cm.yowyob.bus_station_backend.application.dto.voyage.generation.GenerationResultDTO;
import cm.yowyob.bus_station_backend.domain.enums.planning.RecurrenceType;
import org.junit.jupiter.api.*;
import org.springframework.http.MediaType;
import reactor.core.publisher.Mono;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Tests d'intégration - Workflow Planning et Génération de Voyages")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PlanningWorkflowIntegrationTest extends BaseIntegrationTest {

    private UUID agenceId;
    private UUID classVoyageId;
    private UUID vehiculeId;
    private UUID chauffeurId;

    @BeforeEach
    void setUp() {
        // 1. Setup environnement
        UUID orgId = createTestOrganization();
        UUID gareId = createTestGare();
        agenceId = createTestAgenceInDb(orgId, testAdminId, gareId);
        classVoyageId = createTestClassVoyage(agenceId);
        vehiculeId = createTestVehicule(agenceId);
        chauffeurId = createTestChauffeur(agenceId);
    }

    @Test
    @Order(1)
    @DisplayName("Workflow complet : Créer planning → Générer Brouillon → Compléter → Publier")
    void completePlanningAndGenerationWorkflow() {
        // ===== ÉTAPE 1 : Créer une ligne de service (Planning) =====
        CreneauPlanningDTO creneau = CreneauPlanningDTO.builder()
                .jourSemaine(DayOfWeek.MONDAY)
                .heureDepart(LocalTime.of(8, 0))
                .lieuDepart("Yaoundé")
                .lieuArrive("Douala")
                .idClassVoyage(classVoyageId)
                .idVehicule(vehiculeId)
                // On n'affecte pas de chauffeur pour forcer un brouillon INCOMPLET
                .nbrPlacesDisponibles(50)
                .actif(true)
                .build();

        PlanningVoyageDTO planningDTO = PlanningVoyageDTO.builder()
                .idAgenceVoyage(agenceId)
                .nom("Planning Lundi Matin")
                .recurrence(RecurrenceType.HEBDOMADAIRE)
                .dateDebut(LocalDate.now())
                .creneaux(List.of(creneau))
                .build();

        PlanningVoyageDTO createdPlanning = webTestClient.post()
                .uri("/ligne-service")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(planningDTO)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(PlanningVoyageDTO.class)
                .returnResult()
                .getResponseBody();

        assertThat(createdPlanning).isNotNull();
        assertThat(createdPlanning.getIdPlanning()).isNotNull();
        UUID creneauId = createdPlanning.getCreneaux().get(0).getIdCreneau();

        // ===== ÉTAPE 2 : Générer un voyage unitaire (pour un lundi) =====
        LocalDate nextMonday = LocalDate.now().with(java.time.temporal.TemporalAdjusters.nextOrSame(DayOfWeek.MONDAY));
        GenerationUnitaireRequestDTO genRequest = new GenerationUnitaireRequestDTO(creneauId, nextMonday, true);

        GenerationResultDTO genResult = webTestClient.post()
                .uri("/voyage/generer-unitaire")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(genRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody(GenerationResultDTO.class)
                .value(res -> {
                    assertThat(res.getStatut()).isEqualTo("INCOMPLET");
                    assertThat(res.getBrouillonId()).isNotNull();
                    assertThat(res.getConflits()).anyMatch(c -> c.contains("chauffeur"));
                })
                .returnResult()
                .getResponseBody();

        UUID brouillonId = genResult.getBrouillonId();

        // ===== ÉTAPE 3 : Mettre à jour le brouillon pour ajouter le chauffeur et les infos manquantes =====
        VoyageBrouillonUpdateDTO updateDTO = new VoyageBrouillonUpdateDTO();
        updateDTO.setChauffeurId(chauffeurId);
        updateDTO.setVehiculeId(vehiculeId);
        updateDTO.setPrix(5000.0);
        updateDTO.setDateLimiteReservation(nextMonday.atTime(6, 0));
        updateDTO.setDateLimiteConfirmation(nextMonday.atTime(7, 0));
        updateDTO.setPointDeDepart("Mvan");
        updateDTO.setPointArrivee("Bessengue");
        updateDTO.setNbrPlaceReservable(50);
        updateDTO.setTitre("Voyage Yaoundé-Douala");

        webTestClient.put()
                .uri("/voyage/brouillon/{id}", brouillonId)
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updateDTO)
                .exchange()
                .expectStatus().isOk()
                .expectBody(VoyageBrouillonResponseDTO.class)
                .value(res -> {
                    assertThat(res.getChauffeurId()).isEqualTo(chauffeurId);
                    assertThat(res.getStatutBrouillon()).isEqualTo(cm.yowyob.bus_station_backend.domain.enums.StatutBrouillon.PRET);
                });

        // ===== ÉTAPE 4 : Publier le brouillon =====
        webTestClient.post()
                .uri("/voyage/brouillon/{id}/publier", brouillonId)
                .header("Authorization", "Bearer " + adminToken)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.idVoyage").exists()
                .jsonPath("$.statusVoyage").isEqualTo("PUBLIE");
    }

    // ===== Utilitaires =====

    private UUID createTestOrganization() {
        UUID orgId = UUID.randomUUID();
        databaseClient.sql("INSERT INTO organization (id, organization_id, status) VALUES (:id, :orgId, 'ACTIVE')")
                .bind("id", orgId).bind("orgId", orgId).then().block();
        return orgId;
    }

    private UUID createTestGare() {
        UUID gareId = UUID.randomUUID();
        databaseClient.sql("INSERT INTO gare_routiere (id_gare_routiere, nom_gare_routiere, manager_id) VALUES (:id, 'Gare Nord', :managerId)")
                .bind("id", gareId).bind("managerId", testAdminId).then().block();
        return gareId;
    }

    private UUID createTestAgenceInDb(UUID orgId, UUID userId, UUID gareId) {
        UUID agencyId = UUID.randomUUID();
        databaseClient.sql("INSERT INTO agences_voyage (agency_id, organisation_id, user_id, name, location, gare_routiere_id) VALUES (:id, :orgId, :userId, 'Voyage Express', 'Yaoundé', :gareId)")
                .bind("id", agencyId).bind("orgId", orgId).bind("userId", userId).bind("gareId", gareId).then().block();
        return agencyId;
    }

    private UUID createTestClassVoyage(UUID agenceId) {
        UUID classId = UUID.randomUUID();
        databaseClient.sql("INSERT INTO class_voyage (id, label, price, id_agence_voyage, is_active) VALUES (:id, 'Economique', 5000, :agenceId, true)")
                .bind("id", classId).bind("agenceId", agenceId).then().block();
        return classId;
    }

    private UUID createTestVehicule(UUID agenceId) {
        UUID vehId = UUID.randomUUID();
        databaseClient.sql("INSERT INTO vehicules (id_vehicule, nom, nbr_places, id_agence_voyage) VALUES (:id, 'Car-01', 70, :agenceId)")
                .bind("id", vehId).bind("agenceId", agenceId).then().block();
        return vehId;
    }

    private UUID createTestChauffeur(UUID agenceId) {
        UUID userId = UUID.randomUUID();
        UUID chauffId = UUID.randomUUID();
        // Créer l'user d'abord
        databaseClient.sql("INSERT INTO users (user_id, nom, username, email, roles) VALUES (:id, 'Driver', :un, :email, 'EMPLOYE')")
                .bind("id", userId).bind("un", "driver"+chauffId).bind("email", chauffId+"@driver.com").then().block();
        
        databaseClient.sql("INSERT INTO chauffeurs (id, user_id, agence_id, statut) VALUES (:id, :userId, :agenceId, 'LIBRE')")
                .bind("id", chauffId).bind("userId", userId).bind("agenceId", agenceId).then().block();
        return userId;
    }
}
