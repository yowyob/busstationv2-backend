package cm.yowyob.bus_station_backend.integration;

import cm.yowyob.bus_station_backend.BaseIntegrationTest;
import cm.yowyob.bus_station_backend.application.dto.voyage.VoyagePreviewDTO;
import cm.yowyob.bus_station_backend.domain.enums.StatutVoyage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Tests d'intégration - VoyageGareRoutiere")
class VoyageGareRoutiereIntegrationTest extends BaseIntegrationTest {

    @Test
    @DisplayName("Devrait retourner les voyages (départs et arrivées) d'une gare spécifique")
    void shouldReturnVoyagesForSpecificGare() {
        // Given
        String nomGare = "Gare de Mvan";
        UUID gareId = createGareInDb(nomGare);
        
        UUID orgId = createOrganizationInDb();
        UUID agenceId = createAgenceInDb(orgId, testAdminId);
        UUID classId = createClassVoyageInDb();
        UUID vehiculeId = createVehiculeInDb(agenceId);

        // Voyage avec départ de Mvan
        UUID voyage1Id = createVoyageInDb("Voyage Depart Mvan", nomGare, "Douala", nomGare, "Gare de Bessengue");
        createLigneVoyageInDb(voyage1Id, agenceId, classId, vehiculeId);

        // Voyage avec arrivée à Mvan
        UUID voyage2Id = createVoyageInDb("Voyage Arrivee Mvan", "Kribi", nomGare, "Gare de Kribi", nomGare);
        createLigneVoyageInDb(voyage2Id, agenceId, classId, vehiculeId);

        // Voyage sans aucun lien avec Mvan
        UUID voyage3Id = createVoyageInDb("Voyage Autre", "Bafoussam", "Douala", "Gare de Bafoussam", "Gare de Bessengue");
        createLigneVoyageInDb(voyage3Id, agenceId, classId, vehiculeId);

        // When & Then
        webTestClient.get()
                .uri("/voyage/gare/{gareId}", gareId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<RestPageImpl<VoyagePreviewDTO>>() {})
                .value(page -> {
                    assertThat(page.getContent()).hasSize(2);
                    assertThat(page.getContent())
                        .extracting(VoyagePreviewDTO::getLieuDepart)
                        .containsAnyOf(nomGare, "Kribi");
                    assertThat(page.getContent())
                        .extracting(VoyagePreviewDTO::getLieuArrive)
                        .containsAnyOf(nomGare, "Douala");
                });
    }

    @Test
    @DisplayName("Devrait retourner 404 quand la gare n'existe pas")
    void shouldReturn404WhenGareNotFound() {
        webTestClient.get()
                .uri("/voyage/gare/{gareId}", UUID.randomUUID())
                .exchange()
                .expectStatus().isNotFound();
    }

    // ===== Utilitaires de données =====

    private UUID createGareInDb(String nom) {
        UUID id = UUID.randomUUID();
        databaseClient.sql("INSERT INTO gare_routiere (id_gare_routiere, nom_gare_routiere, version) VALUES (:id, :nom, 0)")
                .bind("id", id)
                .bind("nom", nom)
                .then()
                .block();
        return id;
    }

    private UUID createOrganizationInDb() {
        UUID id = UUID.randomUUID();
        databaseClient.sql("INSERT INTO organization (id, long_name, is_active) VALUES (:id, 'Org Test', true)")
                .bind("id", id)
                .then()
                .block();
        return id;
    }

    private UUID createAgenceInDb(UUID orgId, UUID userId) {
        UUID id = UUID.randomUUID();
        databaseClient.sql("INSERT INTO agences_voyage (agency_id, organisation_id, user_id, name, is_active) VALUES (:id, :orgId, :userId, 'Agence Test', true)")
                .bind("id", id)
                .bind("orgId", orgId)
                .bind("userId", userId)
                .then()
                .block();
        return id;
    }

    private UUID createClassVoyageInDb() {
        UUID id = UUID.randomUUID();
        databaseClient.sql("INSERT INTO class_voyage (id, label, price, is_active) VALUES (:id, 'VIP', 5000.0, true)")
                .bind("id", id)
                .then()
                .block();
        return id;
    }

    private UUID createVehiculeInDb(UUID agenceId) {
        UUID id = UUID.randomUUID();
        databaseClient.sql("INSERT INTO vehicules (id_vehicule, nom, nbr_places, id_agence_voyage) VALUES (:id, 'Bus Test', 70, :agenceId)")
                .bind("id", id)
                .bind("agenceId", agenceId)
                .then()
                .block();
        return id;
    }

    private UUID createVoyageInDb(String titre, String lieuDep, String lieuArr, String pointDep, String pointArr) {
        UUID id = UUID.randomUUID();
        databaseClient.sql("""
                INSERT INTO voyages (id_voyage, titre, lieu_depart, lieu_arrive, point_de_depart, point_arrivee, status_voyage, date_publication, date_depart_prev, nbr_place_reservable, nbr_place_restante, nbr_place_reserve, nbr_place_confirm, duree_voyage, amenities)
                VALUES (:id, :titre, :lieuDep, :lieuArr, :pointDep, :pointArr, :status, :now, :now, 50, 50, 0, 0, 3600, 'AC,WIFI')
                """)
                .bind("id", id)
                .bind("titre", titre)
                .bind("lieuDep", lieuDep)
                .bind("lieuArr", lieuArr)
                .bind("pointDep", pointDep)
                .bind("pointArr", pointArr)
                .bind("status", StatutVoyage.PUBLIE.name())
                .bind("now", LocalDateTime.now())
                .then()
                .block();
        return id;
    }

    private void createLigneVoyageInDb(UUID voyageId, UUID agenceId, UUID classId, UUID vehiculeId) {
        UUID id = UUID.randomUUID();
        databaseClient.sql("""
                INSERT INTO lignes_voyage (id_ligne_voyage, id_voyage, id_agence_voyage, id_class_voyage, id_vehicule)
                VALUES (:id, :voyageId, :agenceId, :classId, :vehiculeId)
                """)
                .bind("id", id)
                .bind("voyageId", voyageId)
                .bind("agenceId", agenceId)
                .bind("classId", classId)
                .bind("vehiculeId", vehiculeId)
                .then()
                .block();
    }
}
