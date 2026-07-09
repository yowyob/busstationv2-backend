package cm.yowyob.bus_station_backend.integration;

import cm.yowyob.bus_station_backend.BaseIntegrationTest;
import cm.yowyob.bus_station_backend.application.dto.reservation.*;
import cm.yowyob.bus_station_backend.application.dto.payment.PayRequestDTO;
import cm.yowyob.bus_station_backend.application.dto.payment.PayInResultDTO;
import cm.yowyob.bus_station_backend.application.dto.payment.PayInData;
import cm.yowyob.bus_station_backend.application.dto.payment.ResultStatus;
import cm.yowyob.bus_station_backend.application.dto.payment.PaiementCallbackDTO;
import cm.yowyob.bus_station_backend.application.dto.voyage.VoyageCancelDTO;
import cm.yowyob.bus_station_backend.domain.enums.*;
import cm.yowyob.bus_station_backend.domain.model.Reservation;
import org.junit.jupiter.api.*;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@DisplayName("Tests d'intégration - Workflow complet de réservation")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ReservationWorkflowIntegrationTest extends BaseIntegrationTest {

    private UUID voyageId;
    private UUID reservationId;
    private UUID agenceId;

    @BeforeEach
    void setUp() {
        // Mock Payment initiation
        PayInResultDTO mockPayResult = new PayInResultDTO();
        mockPayResult.setStatus(ResultStatus.SUCCESS);
        mockPayResult.setOk(true);
        PayInData data = new PayInData();
        data.setTransaction_code("TX-" + UUID.randomUUID().toString().substring(0, 8));
        mockPayResult.setData(data);
        when(paymentPort.initiatePayment(anyString(), anyString(), anyDouble(), any())).thenReturn(Mono.just(mockPayResult));

        // Préparer les données de test
        UUID organizationId = createTestOrganization();
        agenceId = createTestAgence(organizationId);
        UUID classVoyageId = createTestClassVoyage(agenceId);
        UUID vehiculeId = createTestVehicule(agenceId);
        voyageId = createTestVoyage();
        createLigneVoyage(voyageId, classVoyageId, vehiculeId, agenceId);
        createTestPolitiqueAnnulation(agenceId);
    }

    private void createTestPolitiqueAnnulation(UUID agenceId) {
        databaseClient.sql("""
            INSERT INTO politiques_annulation (id_politique, duree_coupon_seconds, id_agence_voyage)
            VALUES (:id, :duree, :agenceId)
        """)
                .bind("id", UUID.randomUUID())
                .bind("duree", 3600L)
                .bind("agenceId", agenceId)
                .then()
                .block();
    }

    @Test
    @Order(1)
    @DisplayName("Scénario complet : Créer réservation → Initier Paiement → Webhook → Vérifier")
    void completeReservationWorkflow() {
        // ===== ÉTAPE 1 : Créer une réservation =====
        ReservationDTO reservationDTO = createReservationDTO();

        Reservation createdReservation = webTestClient.post()
                .uri("/reservation/reserver")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(reservationDTO)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Reservation.class)
                .returnResult()
                .getResponseBody();

        assertThat(createdReservation).isNotNull();
        assertThat(createdReservation.getStatutReservation()).isEqualTo(StatutReservation.RESERVER);
        assertThat(createdReservation.getNbrPassager()).isEqualTo(2);

        reservationId = createdReservation.getIdReservation();

        // Vérifier que les places ont été réservées dans le voyage
        verifyVoyagePlacesReduced(voyageId, 2);

        // ===== ÉTAPE 2 : Initier le paiement =====
        PayRequestDTO payRequest = new PayRequestDTO();
        payRequest.setReservationId(reservationId);
        payRequest.setAmount(50000.0);
        payRequest.setMobilePhone("677777777");
        payRequest.setMobilePhoneName("MTN Mobile Money");

        PayInResultDTO payResult = webTestClient.post()
                .uri("/paiement/initier")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody(PayInResultDTO.class)
                .returnResult()
                .getResponseBody();

        assertThat(payResult).isNotNull();
        String transactionCode = payResult.getData().getTransaction_code();

        // ===== ÉTAPE 3 : Simuler le Webhook de confirmation (Callback Opérateur) =====
        PaiementCallbackDTO callback = new PaiementCallbackDTO();
        callback.setTransactionCode(transactionCode);
        callback.setReservationId(reservationId);
        callback.setMontantPaye(50000.0);

        webTestClient.post()
                .uri("/paiement/confirmer")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(callback)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ReservationDetailDTO.class)
                .value(confirmed -> {
                    assertThat(confirmed.getReservation().getStatutReservation()).isEqualTo(StatutReservation.CONFIRMER);
                    assertThat(confirmed.getReservation().getStatutPayement()).isEqualTo(StatutPayment.PAID);
                });
    }

    @Test
    @Order(2)
    @DisplayName("Scénario d'annulation : Créer → Annuler → Vérifier places")
    void cancellationWorkflow() {
        // ÉTAPE 1 : Créer une réservation
        ReservationDTO reservationDTO = createReservationDTO();

        Reservation createdReservation = webTestClient.post()
                .uri("/reservation/reserver")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(reservationDTO)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Reservation.class)
                .returnResult()
                .getResponseBody();

        UUID resId = createdReservation.getIdReservation();

        // Récupérer les IDs des passagers pour l'annulation
        List<UUID> passagerIds = databaseClient
                .sql("SELECT id_passager FROM passagers WHERE id_reservation = :id")
                .bind("id", resId)
                .map(row -> row.get("id_passager", UUID.class))
                .all()
                .collectList()
                .block();

        ReservationCancelDTO cancelDTO = new ReservationCancelDTO();
        cancelDTO.setIdReservation(resId);
        cancelDTO.setIdPassagers(passagerIds.toArray(new UUID[0]));
        cancelDTO.setCanceled(true);

        // ÉTAPE 2 : Annuler la réservation
        webTestClient.post()
                .uri("/reservation/annuler/{reservationId}", resId)
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(cancelDTO)
                .exchange()
                .expectStatus().isNoContent();

        // ÉTAPE 3 : Vérifier que les places sont libérées
        verifyVoyagePlacesIncreased(voyageId, 2);
    }

    @Test
    @Order(3)
    @DisplayName("Scénario d'échec : Réserver plus de places que disponible")
    void shouldFailWhenOverbooking() {
        // Given - Créer une réservation qui dépasse la capacité
        ReservationDTO reservationDTO = createReservationDTO();
        reservationDTO.setNbrPassager(100);
        webTestClient.post()
                .uri("/reservation/reserver")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(reservationDTO)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    @Order(5)
    @DisplayName("Scénario : Lister les réservations d'un utilisateur")
    void shouldListUserReservations() {
        // Given - Créer plusieurs réservations
        for (int i = 0; i < 3; i++) {
            ReservationDTO reservationDTO = createReservationDTO();
            webTestClient.post()
                    .uri("/reservation/reserver")
                    .header("Authorization", "Bearer " + userToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(reservationDTO)
                    .exchange()
                    .expectStatus().isCreated();
        }

        // When & Then
        webTestClient.get()
                .uri("/reservation/user/{userId}", testUserId)
                .header("Authorization", "Bearer " + userToken)
                .exchange()
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<RestPageImpl<ReservationPreviewDTO>>() {})
                .value(page -> {
                    assertThat(page.getContent()).hasSizeGreaterThanOrEqualTo(3);
                });
    }

    @Test
    @Order(6)
    @DisplayName("Scénario : Annulation par l'agence")
    void agencyCancellationWorkflow() {
        // Given - Créer une réservation
        ReservationDTO reservationDTO = createReservationDTO();

        Reservation createdReservation = webTestClient.post()
                .uri("/reservation/reserver")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(reservationDTO)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Reservation.class)
                .returnResult()
                .getResponseBody();

        // L'agence annule le voyage
        VoyageCancelDTO cancelDTO = new VoyageCancelDTO();
        cancelDTO.setIdVoyage(voyageId);
        cancelDTO.setAgenceVoyageId(agenceId);
        cancelDTO.setCauseAnnulation("Problème technique");
        cancelDTO.setCanceled(true);

        // Utiliser le token admin (chef d'agence)
        webTestClient.post()
                .uri("/reservation/agence/annuler-voyage")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(cancelDTO)
                .exchange()
                .expectStatus().isNoContent();

        // Vérifier que les places sont libérées
        verifyVoyagePlacesIncreased(voyageId, 2);
    }

    // ===== Méthodes utilitaires =====

    private ReservationDTO createReservationDTO() {
        ReservationDTO dto = new ReservationDTO();
        dto.setIdVoyage(voyageId);
        dto.setNbrPassager(2);
        dto.setMontantPaye(50000);

        PassagerDTO passager1 = new PassagerDTO();
        passager1.setNom("Doe");
        passager1.setGenre("MALE");
        passager1.setNbrBaggage(3);
        passager1.setPlaceChoisis(1);

        PassagerDTO passager2 = new PassagerDTO();
        passager2.setNom("Doe");
        passager2.setGenre("FEMALE");
        passager2.setNbrBaggage(2);
        passager2.setPlaceChoisis(2);

        dto.setPassagerDTO(new PassagerDTO[]{passager1, passager2});

        return dto;
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

    private UUID createTestAgence(UUID organizationId) {
        UUID agencyId = UUID.randomUUID();
        databaseClient
                .sql("""
            INSERT INTO agences_voyage
            (agency_id, organisation_id, user_id, name, short_name, location)
            VALUES (:agencyId, :orgId, :userId, :longName, :shortName, :location)
        """)
                .bind("agencyId", agencyId)
                .bind("orgId", organizationId)
                .bind("userId", testAdminId)
                .bind("longName", "Agence Test")
                .bind("shortName", "AT-" + UUID.randomUUID().toString().substring(0, 5))
                .bind("location", "Yaoundé")
                .then()
                .block();
        return agencyId;
    }

    private UUID createTestClassVoyage(UUID agenceId) {
        UUID classId = UUID.randomUUID();
        databaseClient
                .sql("""
            INSERT INTO class_voyage
            (id, label, price, id_agence_voyage)
            VALUES (:id, :nom, :prix, :agenceId)
        """)
                .bind("id", classId)
                .bind("nom", "Économique")
                .bind("prix", 25000.0)
                .bind("agenceId", agenceId)
                .fetch()
                .rowsUpdated()
                .block();

        return classId;
    }

    private UUID createTestVehicule(UUID agenceId) {
        UUID vehiculeId = UUID.randomUUID();
        databaseClient
                .sql("""
                INSERT INTO vehicules 
                (id_vehicule, nom, modele, nbr_places, plaque_matricule, id_agence_voyage)
                VALUES (:id, :nom, :modele, :places, :plaque, :agenceId)
                """)
                .bind("id", vehiculeId)
                .bind("nom", "Bus Test")
                .bind("modele", "Mercedes Sprinter")
                .bind("places", 50)
                .bind("plaque", "TEST-" + UUID.randomUUID().toString().substring(0, 6))
                .bind("agenceId", agenceId)
                .fetch()
                .rowsUpdated()
                .block();
        return vehiculeId;
    }

    private UUID createTestVoyage() {
        UUID voyageId = UUID.randomUUID();
        LocalDateTime departureDate = LocalDateTime.now().plusDays(2);
        LocalDateTime reservationDeadline = LocalDateTime.now().plusDays(1);

        databaseClient
                .sql("""
                INSERT INTO voyages 
                (id_voyage, titre, description, date_depart_prev, lieu_depart, lieu_arrive,
                 nbr_place_reservable, nbr_place_restante, date_limite_reservation, 
                 date_limite_confirmation, status_voyage)
                VALUES (:id, :titre, :desc, :dateDepart, :lieuDepart, :lieuArrive,
                        :places, :placesRestantes, :deadline, :confirmDeadline, :status)
                """)
                .bind("id", voyageId)
                .bind("titre", "Yaoundé - Douala Test")
                .bind("desc", "Voyage de test")
                .bind("dateDepart", departureDate)
                .bind("lieuDepart", "Yaoundé")
                .bind("lieuArrive", "Douala")
                .bind("places", 50)
                .bind("placesRestantes", 50)
                .bind("deadline", reservationDeadline)
                .bind("confirmDeadline", departureDate.minusHours(2))
                .bind("status", "PUBLIE")
                .fetch()
                .rowsUpdated()
                .block();
        return voyageId;
    }

    private void createLigneVoyage(UUID voyageId, UUID classVoyageId, UUID vehiculeId, UUID agenceId) {
        databaseClient
                .sql("""
                INSERT INTO lignes_voyage 
                (id_ligne_voyage, id_class_voyage, id_vehicule, id_voyage, id_agence_voyage)
                VALUES (:id, :classId, :vehiculeId, :voyageId, :agenceId)
                """)
                .bind("id", UUID.randomUUID())
                .bind("classId", classVoyageId)
                .bind("vehiculeId", vehiculeId)
                .bind("voyageId", voyageId)
                .bind("agenceId", agenceId)
                .fetch()
                .rowsUpdated()
                .block();
    }

    private void verifyVoyagePlacesReduced(UUID voyageId, int expectedReduction) {
        Integer placesRestantes = databaseClient
                .sql("SELECT nbr_place_reservable FROM voyages WHERE id_voyage = :id")
                .bind("id", voyageId)
                .map(row -> row.get("nbr_place_reservable", Integer.class))
                .one()
                .block();

        assertThat(placesRestantes).isEqualTo(50 - expectedReduction);
    }

    private void verifyVoyagePlacesIncreased(UUID voyageId, int expectedIncrease) {
        Integer placesRestantes = databaseClient
                .sql("SELECT nbr_place_reservable FROM voyages WHERE id_voyage = :id")
                .bind("id", voyageId)
                .map(row -> row.get("nbr_place_reservable", Integer.class))
                .one()
                .block();

        assertThat(placesRestantes).isGreaterThanOrEqualTo(50);
    }
}
