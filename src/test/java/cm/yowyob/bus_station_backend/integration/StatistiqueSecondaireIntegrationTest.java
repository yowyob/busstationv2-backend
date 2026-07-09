package cm.yowyob.bus_station_backend.integration;

import cm.yowyob.bus_station_backend.BaseIntegrationTest;
import cm.yowyob.bus_station_backend.domain.enums.StatutVoyage;
import cm.yowyob.bus_station_backend.domain.enums.StatutReservation;
import org.junit.jupiter.api.*;
import org.springframework.http.MediaType;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.hamcrest.Matchers.*;

@DisplayName("Tests d'intégration - Workflow Statistiques et Éléments Secondaires")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class StatistiqueSecondaireIntegrationTest extends BaseIntegrationTest {

    private UUID agencyId;
    private UUID voyageId;

    @BeforeEach
    void setUp() {
        agencyId = UUID.randomUUID();
        voyageId = UUID.randomUUID();

        // 1. Agence
        databaseClient.sql("INSERT INTO agences_voyage (agency_id, name, is_active) VALUES (:id, 'Test Agency', true)")
                .bind("id", agencyId)
                .then().block();

        // 2. Véhicules (pour les stats)
        databaseClient.sql("INSERT INTO vehicules (id_vehicule, nom, nbr_places, id_agence_voyage) VALUES (:id, 'Bus 1', 50, :agencyId)")
                .bind("id", UUID.randomUUID())
                .bind("agencyId", agencyId)
                .then().block();

        // 3. Voyage
        databaseClient.sql("""
                INSERT INTO voyages (id_voyage, titre, status_voyage, date_depart_prev, date_publication, nbr_place_reservable, nbr_place_reserve) 
                VALUES (:id, 'Test Voyage', 'PUBLIE', :depDate, :pubDate, 40, 10)
                """)
                .bind("id", voyageId)
                .bind("depDate", LocalDateTime.now().plusDays(2))
                .bind("pubDate", LocalDateTime.now().minusDays(1))
                .then().block();

        // 4. Ligne Voyage (lien Agence <-> Voyage)
        databaseClient.sql("INSERT INTO lignes_voyage (id_ligne_voyage, id_voyage, id_agence_voyage) VALUES (:id, :voyId, :agenceId)")
                .bind("id", UUID.randomUUID())
                .bind("voyId", voyageId)
                .bind("agenceId", agencyId)
                .then().block();

        // 5. Réservations (pour les stats de revenus et comptes)
        UUID resId1 = UUID.randomUUID();
        databaseClient.sql("""
                INSERT INTO reservations (id_reservation, id_voyage, id_user, statut_reservation, prix_total, montant_paye, date_reservation, date_confirmation) 
                VALUES (:id, :voyId, :userId, 'CONFIRMER', 5000.0, 5000.0, :now, :now)
                """)
                .bind("id", resId1)
                .bind("voyId", voyageId)
                .bind("userId", testUserId)
                .bind("now", LocalDateTime.now())
                .then().block();

        UUID resId2 = UUID.randomUUID();
        databaseClient.sql("""
                INSERT INTO reservations (id_reservation, id_voyage, id_user, statut_reservation, prix_total, montant_paye, date_reservation) 
                VALUES (:id, :voyId, :userId, 'ANNULER', 5000.0, 0.0, :now)
                """)
                .bind("id", resId2)
                .bind("voyId", voyageId)
                .bind("userId", testUserId)
                .bind("now", LocalDateTime.now())
                .then().block();

        // 6. Solde Indemnisation
        databaseClient.sql("INSERT INTO soldes_indemnisation (id_solde, solde, type, id_user, id_agence_voyage) VALUES (:id, 1500.0, 'CASHBACK', :userId, :agenceId)")
                .bind("id", UUID.randomUUID())
                .bind("userId", testUserId)
                .bind("agenceId", agencyId)
                .then().block();
    }

    @Test
    @Order(1)
    @DisplayName("Voyages similaires - Validation des données")
    void getVoyagesSimilaires() {
        authenticatedClient(userToken).get()
                .uri("/voyage/{id}/similaires", voyageId)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$").isArray()
                .jsonPath("$.length()").value(greaterThanOrEqualTo(0));
    }

    @Test
    @Order(2)
    @DisplayName("Statistiques Générales Agence - Validation des calculs")
    void getStatistiquesGenerales() {
        authenticatedClient(adminToken).get()
                .uri("/statistiques/agence/{id}/general", agencyId)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.nombreVoyages").isEqualTo(1)
                .jsonPath("$.nombreReservations").isEqualTo(2)
                .jsonPath("$.revenus").isEqualTo(5000.0)
                .jsonPath("$.voyagesParStatut.PUBLIE").isEqualTo(1)
                .jsonPath("$.reservationsParStatut.CONFIRMER").isEqualTo(1)
                .jsonPath("$.reservationsParStatut.ANNULER").isEqualTo(1)
                .jsonPath("$.tauxOccupation").isEqualTo(20.0); // 10 / (40+10) * 100
    }

    @Test
    @Order(3)
    @DisplayName("Évolutions Statistiques Agence - Validation du format")
    void getEvolutionStatistiques() {
        authenticatedClient(adminToken).get()
                .uri("/statistiques/agence/{id}/evolution", agencyId)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.evolutionReservations").isArray()
                .jsonPath("$.evolutionVoyages").isArray()
                .jsonPath("$.evolutionRevenus").isArray();
    }

    @Test
    @Order(4)
    @DisplayName("Solde Indemnisation Utilisateur - Validation du montant")
    void getSoldeIndemnisation() {
        authenticatedClient(userToken).get()
                .uri("/solde-indemnisation/user/{userId}", testUserId)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$").isArray()
                .jsonPath("$[0].solde").isEqualTo(1500.0)
                .jsonPath("$[0].idUser").isEqualTo(testUserId.toString());
    }

    @Test
    @Order(5)
    @DisplayName("Statistiques Complètes - Validation agrégation")
    void getCompleteStatistics() {
        authenticatedClient(adminToken).get()
                .uri("/statistiques/agence/{id}/complete", agencyId)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.general").exists()
                .jsonPath("$.evolution").exists()
                .jsonPath("$.general.revenus").isEqualTo(5000.0);
    }
}
