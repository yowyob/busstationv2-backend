package cm.yowyob.bus_station_backend.application.service;

import cm.yowyob.bus_station_backend.application.dto.payment.TransactionStatus;
import cm.yowyob.bus_station_backend.application.dto.reservation.*;
import cm.yowyob.bus_station_backend.application.dto.payment.PayRequestDTO;
import cm.yowyob.bus_station_backend.application.dto.payment.PayInResultDTO;
import cm.yowyob.bus_station_backend.application.dto.payment.ResultStatus;
import cm.yowyob.bus_station_backend.application.mapper.ReservationMapper;
import cm.yowyob.bus_station_backend.application.port.out.*;
import cm.yowyob.bus_station_backend.application.port.in.ReservationUseCase;
import cm.yowyob.bus_station_backend.domain.enums.*;
import cm.yowyob.bus_station_backend.domain.exception.ReservationException;
import cm.yowyob.bus_station_backend.domain.exception.ResourceNotFoundException;
import cm.yowyob.bus_station_backend.domain.model.*;
import cm.yowyob.bus_station_backend.domain.factory.NotificationFactory; // À adapter selon ton factory existant
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationService implements ReservationUseCase {

        private final ReservationPersistencePort reservationPort;
        private final VoyagePersistencePort voyagePort;
        private final AgencePersistencePort agencePort;
        private final PaymentPort paymentPort;
        private final NotificationPort notificationPort;
        private final ReservationMapper reservationMapper;
        private final TransactionalOperator rxtx; // Gestionnaire de transaction Réactif

        // Cache local pour les verrous temporaires (WebSocket).
        // Idéalement, à remplacer par RedisPersistencePort pour scaler.
        private final Map<UUID, Set<Integer>> reservedSeatsMap = new ConcurrentHashMap<>();

        @Override
        public Mono<ReservationDetailDTO> getReservationDetails(UUID reservationId) {

                return reservationPort.findById(reservationId)
                                .switchIfEmpty(Mono.error(
                                                new ResourceNotFoundException("Réservation non trouvée")))
                                .flatMap(reservation -> Mono.zip(
                                                reservationPort.findPassagersByReservationId(
                                                                reservation.getIdReservation()).collectList(),
                                                voyagePort.findById(reservation.getIdVoyage()))
                                                .flatMap(tuple -> {
                                                        List<Passager> passagers = tuple.getT1();
                                                        Voyage voyage = tuple.getT2();
                                                        return voyagePort
                                                                        .findLigneVoyageByVoyageId(voyage.getIdVoyage())
                                                                        .flatMap(ligne -> Mono.zip(
                                                                                        agencePort.findById(ligne
                                                                                                        .getIdAgenceVoyage()),
                                                                                        agencePort.findVehiculeById(
                                                                                                        ligne.getIdVehicule())))
                                                                        .map(tuple2 -> reservationMapper.toDetailDTO(
                                                                                        reservation, passagers, voyage,
                                                                                        tuple2.getT1(), // Agence
                                                                                        tuple2.getT2() // Vehicule
                                                        ));
                                                }));
        }

        @Override
        public Mono<Page<ReservationPreviewDTO>> getReservationsByUser(UUID userId, Pageable pageable) {
                return reservationPort.findByUserId(userId, pageable)
                                .flatMap(this::enrichReservationPreview)
                                .collectList()
                                .zipWith(reservationPort.countByUserId(userId))
                                .map(tuple -> new PageImpl<>(tuple.getT1(), pageable, tuple.getT2()));
        }

        @Override
        public Mono<Page<ReservationPreviewDTO>> getReservationsByAgence(UUID agenceId, Pageable pageable) {
                return reservationPort.findByAgenceId(agenceId, pageable)
                                .flatMap(this::enrichReservationPreview)
                                .collectList()
                                .zipWith(reservationPort.countReservationsByAgenceId(agenceId))
                                .map(tuple -> new PageImpl<>(tuple.getT1(), pageable, tuple.getT2()));
        }

        @Override
        public Mono<Page<ReservationPreviewDTO>> getAllReservations(Pageable pageable) {
                // 1. Récupérer les réservations paginées
                return reservationPort.findAll(pageable)
                                .flatMap(this::enrichReservationPreview)
                                .collectList()
                                .zipWith(reservationPort.countAllReservations())
                                .map(tuple -> new PageImpl<>(tuple.getT1(), pageable, tuple.getT2()));
        }

        // --- SAGA CREATE RESERVATION ---
        @Override
        public Mono<ReservationDetailDTO> confirmer(PayRequestDTO payRequestDTO) {
                return confirmReservation(new ReservationConfirmDTO(
                        payRequestDTO.getReservationId(),
                        payRequestDTO.getAmount()
                )).flatMap(reservation -> getReservationDetails(reservation.getIdReservation()));
        }

        @Override
        public Mono<ResultStatus> getPaymentStatus(String transactionCode) {
                return paymentPort.checkPaymentStatus(transactionCode)
                        .map(cm.yowyob.bus_station_backend.application.dto.payment.StatusResultDTO::getStatus);
        }

        @Override
        public Mono<Reservation> createReservation(ReservationDTO dto) {
                // Validation basique
                if (dto.getPassagerDTO() == null || dto.getPassagerDTO().length == 0) {
                        return Mono.error(new ReservationException("La liste des passagers ne peut pas être vide"));
                }

                UUID voyageId = dto.getIdVoyage();
                int nbrPassagers = dto.getNbrPassager();

                // 1. Chargement du Voyage (État initial)
                return voyagePort.findById(voyageId)
                                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Voyage introuvable")))
                                .flatMap(voyage -> {

                                        log.info("Voyage found: " + voyage.getIdVoyage() + ", places reservable: " + voyage.getNbrPlaceReservable());

                                        // 2. Vérification Règles Métier (Domaine)
                                        try {
                                                voyage.reserverPlaces(nbrPassagers, LocalDateTime.now()); // Vérifie
                                                                                                          // dates et
                                                                                                          // capacité
                                                                                                          // mémoire
                                             log.info("After in-memory reservation: " + voyage.getIdVoyage() + ", places reservable: " + voyage.getNbrPlaceReservable());

                                        } catch (ReservationException e) {
                                             log.error("ReservationException in-memory: " + e.getMessage());
                                                return Mono.error(e);
                                        }

                                        // Récupération du prix via la classe de voyage
                                        return voyagePort.findLigneVoyageByVoyageId(voyageId)
                                                        .flatMap(ligne -> voyagePort
                                                                        .findClassVoyageById(ligne.getIdClassVoyage()))
                                                        .flatMap(classeVoyage -> {

                                                                double prixTotal = classeVoyage.getPrix()
                                                                                * nbrPassagers;

                                                                // 3. Construction des Objets du Domaine
                                                                Reservation reservation = Reservation.builder()
                                                                                .idReservation(UUID.randomUUID())
                                                                                .idUser(dto.getIdUser())
                                                                                .idVoyage(voyageId)
                                                                                .nbrPassager(nbrPassagers)
                                                                                .dateReservation(LocalDateTime.now())
                                                                                .statutReservation(
                                                                                                StatutReservation.RESERVER)
                                                                                .statutPayement(StatutPayment.NO_PAYMENT)
                                                                                .prixTotal(prixTotal)
                                                                                .montantPaye(0.0)
                                                                                .build();

                                                                List<Passager> passagers = Arrays
                                                                                .stream(dto.getPassagerDTO())
                                                                                .map(pDto -> Passager.builder()
                                                                                                .idPassager(UUID.randomUUID())
                                                                                                .idReservation(reservation
                                                                                                                .getIdReservation())
                                                                                                .nom(pDto.getNom())
                                                                                                .genre(Gender.valueOf(
                                                                                                                pDto.getGenre()))
                                                                                                .age(pDto.getAge())
                                                                                                .nbrBaggage(pDto.getNbrBaggage())
                                                                                                .numeroPieceIdentific(
                                                                                                                pDto.getNumeroPieceIdentific())
                                                                                                .placeChoisis(pDto
                                                                                                                .getPlaceChoisis())
                                                                                                .build())
                                                                                .collect(Collectors.toList());

                                                                Historique historique = Historique.builder()
                                                                                .idHistorique(UUID.randomUUID())
                                                                                .idReservation(reservation
                                                                                                .getIdReservation())
                                                                                .dateReservation(LocalDateTime.now())
                                                                                .statusHistorique(
                                                                                                StatutHistorique.VALIDER)
                                                                                .build();

                                                                // 4. Exécution de la Saga Transactionnelle
                                                                return executeCreationSaga(reservation, passagers,
                                                                                historique, voyageId, nbrPassagers);
                                                        });
                                });
        }

        /**
         * Orchestration de la Saga pour la création :
         * 1. Décrémentation atomique des places (DB Lock).
         * 2. Sauvegarde Réservation, Passagers, Historique.
         * 3. Notification.
         * 4. Rollback automatique via rxtx si erreur.
         */
        private Mono<Reservation> executeCreationSaga(Reservation reservation, List<Passager> passagers,
                        Historique historique, UUID voyageId, int nbrPassagers) {

                // Step 1: Réservation atomique des places en DB
                return reservationPort.decrementPlacesVoyage(voyageId, nbrPassagers)
                                .filter(Boolean::booleanValue)
                                .switchIfEmpty(Mono.error(new ReservationException(
                                                "Plus de places disponibles (Concurrency check)")))
                                .flatMap(success ->
                                // Step 2: Persistance des données
                                reservationPort.save(reservation)
                                                .flatMap(savedRes -> reservationPort.savePassagers(passagers)
                                                                .then(Mono.just(savedRes)))
                                                .flatMap(savedRes -> reservationPort.saveHistorique(historique)
                                                                .then(Mono.just(savedRes))))
                                // Step 3: Notification (Side Effect post-commit idéalement, mais ici in-flow
                                // pour simplicité)
                                .flatMap(savedRes -> voyagePort.findLigneVoyageByVoyageId(voyageId)
                                                .flatMap(ligne -> voyagePort.findById(voyageId)
                                                                .flatMap(voyage -> notificationPort.sendNotification(
                                                                                NotificationFactory
                                                                                                .createReservationCreatedEvent(
                                                                                                                savedRes,
                                                                                                                voyage,
                                                                                                                ligne.getIdAgenceVoyage()))))
                                                .thenReturn(savedRes))
                                // Gestion Transactionnelle R2DBC (Remplace la compensation manuelle si on est
                                // sur la même DB)
                                .as(rxtx::transactional)
                                // Compensation explicite (Saga pattern) si la transaction R2DBC ne couvre pas
                                // tout (ex: appel API externe)
                                .onErrorResume(e -> {
                                        log.error("Erreur lors de la création de la réservation : {}", e.getMessage());
                                        // Si on avait fait des appels API externes ici, il faudrait les annuler
                                        // manuellement.
                                        // Comme tout est DB ici, le rollback R2DBC suffit, mais on pourrait loguer la
                                        // compensation.
                                        return Mono.error(e);
                                });
        }

        @Override
        public Mono<Reservation> confirmReservation(ReservationConfirmDTO confirmDTO) {
                return reservationPort.findById(confirmDTO.getIdReservation())
                                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Réservation introuvable")))
                                .flatMap(reservation -> voyagePort.findById(reservation.getIdVoyage())
                                                .flatMap(voyage -> {
                                                        // Logique métier de confirmation
                                                        reservation.confirmer(confirmDTO.getMontantPaye());
                                                        voyage.confirmerPlaces(reservation.getNbrPassager()); // Mise à
                                                                                                              // jour
                                                                                                              // stats
                                                                                                              // voyage

                                                        return Mono.when(
                                                                        reservationPort.save(reservation),
                                                                        voyagePort.save(voyage), // Sauvegarde les stats
                                                                                                 // places confirmées
                                                                        notificationPort.sendNotification(
                                                                                        NotificationFactory
                                                                                                        .createReservationConfirmedEvent(
                                                                                                                        reservation,
                                                                                                                        voyage)))
                                                                        .thenReturn(reservation);
                                                }))
                                .as(rxtx::transactional);
        }

        // --- PAIEMENT ---

        @Override
        public Mono<PayInResultDTO> initiatePayment(PayRequestDTO request) {
                return reservationPort.findById(request.getReservationId())
                                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Réservation introuvable")))
                                .flatMap(reservation -> {
                                        // Appel Port Paiement
                                        return paymentPort.initiatePayment(
                                                        request.getMobilePhone(),
                                                        request.getMobilePhoneName(),
                                                        request.getAmount(), // Ou reservation.getPrixTotal() pour
                                                                             // forcer le montant
                                                        reservation.getIdUser()).flatMap(result -> {
                                                                // Mise à jour statut si succès initial
                                                                if (result.getStatus() == cm.yowyob.bus_station_backend.application.dto.payment.ResultStatus.SUCCESS) {
                                                                        reservation.initierPaiement(result.getData()
                                                                                        .getTransaction_code());
                                                                        return reservationPort.save(reservation)
                                                                                        .thenReturn(result);
                                                                }
                                                                return Mono.just(result);
                                                        });
                                });
        }

        @Override
        public Mono<Void> processPaymentStatusCheck() {
                return reservationPort.findPendingReservations()
                                .filter(res -> res.getStatutPayement() == StatutPayment.PENDING)
                                .flatMap(reservation -> paymentPort.checkPaymentStatus(reservation.getTransactionCode())
                                                .flatMap(statusResult -> {
                                                        if (statusResult.getData()
                                                                        .getStatus() == TransactionStatus.COMPLETED) {
                                                                return confirmReservation(new ReservationConfirmDTO(
                                                                                reservation.getIdReservation(),
                                                                                statusResult.getData()
                                                                                                .getTransaction_amount()))
                                                                                .then();
                                                        } else if (statusResult.getStatus() == ResultStatus.FAILED) {
                                                                reservation.setStatutPayement(StatutPayment.FAILED);
                                                                return reservationPort.save(reservation)
                                                                                .flatMap(r -> notificationPort
                                                                                                .sendNotification(
                                                                                                                NotificationFactory
                                                                                                                                .createPaymentFailedEvent(
                                                                                                                                                r)))
                                                                                .then();
                                                        }
                                                        return Mono.empty();
                                                }))
                                .then();
        }

        // --- BILLETTERIE ---
        @Override
        public Mono<BilletDTO> generateBillet(UUID passagerId) {
                return reservationPort.findPassagerById(passagerId)
                                .switchIfEmpty(Mono.error(
                                                new ResourceNotFoundException("Passager introuvable")))
                                .flatMap(passager -> reservationPort.findById(passager.getIdReservation())
                                                .flatMap(reservation -> Mono.zip(
                                                                voyagePort.findById(reservation.getIdVoyage()),
                                                                voyagePort.findLigneVoyageByVoyageId(
                                                                                reservation.getIdVoyage()))
                                                                .flatMap(tuple -> {
                                                                        Voyage voyage = tuple.getT1();
                                                                        LigneVoyage ligne = tuple.getT2();

                                                                        return Mono.zip(
                                                                                        agencePort.findById(ligne
                                                                                                        .getIdAgenceVoyage()),
                                                                                        voyagePort.findClassVoyageById(
                                                                                                        ligne.getIdClassVoyage()))
                                                                                        .map(tuple2 -> reservationMapper
                                                                                                        .toBilletDTO(
                                                                                                                        reservation,
                                                                                                                        passager,
                                                                                                                        voyage,
                                                                                                                        tuple2.getT2(), // Classe
                                                                                                                        tuple2.getT1() // Agence
                                                                        ));
                                                                })));
        }

        // --- GESTION PLACES (TEMPS RÉEL) ---
        @Override
        public Flux<PlaceReservationResponse> handlePlaceSelection(UUID voyageId, PlaceReservationRequest request) {
                return Mono.fromCallable(() -> {
                        Set<Integer> reserved = reservedSeatsMap
                                        .computeIfAbsent(voyageId, k -> ConcurrentHashMap.newKeySet());

                        int place = request.getPlaceNumber();

                        if (request.getStatus() == PlaceStatus.RESERVED) {
                                if (!reserved.add(place)) {
                                        throw new ReservationException("Place déjà réservée");
                                }
                        } else {
                                reserved.remove(place);
                        }

                        return reserved;
                }).flatMapMany(set -> Flux.fromIterable(set)
                                .map(p -> new PlaceReservationResponse(p, PlaceStatus.RESERVED)));
        }

        @Override
        public Flux<Integer> getOccupiedAndReservedPlaces(UUID voyageId) {
                return Flux.merge(
                                // Places confirmées (occupées)
                                reservationPort.findConfirmedPassagersPlaces(voyageId),

                                // Places réservées mais non encore confirmées
                                reservationPort.findReservedPassagersPlaces(voyageId),

                                // Places temporairement réservées (cache local)
                                Flux.fromIterable(reservedSeatsMap.getOrDefault(voyageId, Collections.emptySet())))
                                .distinct() // Éviter les doublons si une place est dans plusieurs catégories
                                .sort(); // Trier par ordre numérique
        }

        @Override
        public Flux<PassagerDTO> getAllPassagersByReservation() {
                return null;
        }

        @Override
        public Mono<PassagerDTO> getPassagerById(UUID passagerId) {
                return null;
        }

        @Override
        public Mono<PassagerDTO> updatePassager(UUID passagerId, PassagerDTO dto) {
                return null;
        }

        @Override
        public Mono<Void> deletePassager(UUID passagerId) {
                return null;
        }

        @Override
        public Flux<BaggageDTO> getAllBagagesByReservation() {
                return null;
        }

        @Override
        public Mono<BaggageDTO> getBaggageById(UUID baggageId) {
                return null;
        }

        @Override
        public Mono<BaggageDTO> createBaggage(BaggageDTO dto) {
                return null;
        }

        @Override
        public Mono<BaggageDTO> updateBaggage(UUID baggageId, BaggageDTO dto) {
                return null;
        }

        @Override
        public Mono<Void> deleteBaggage(UUID baggageId) {
                return null;
        }

        // Helper
        private Mono<ReservationPreviewDTO> enrichReservationPreview(Reservation reservation) {
                return voyagePort.findById(reservation.getIdVoyage())
                                .flatMap(voyage -> voyagePort.findLigneVoyageByVoyageId(voyage.getIdVoyage())
                                                .flatMap(ligne -> agencePort.findById(ligne.getIdAgenceVoyage()))
                                                .map(agence -> reservationMapper.toPreviewDTO(reservation, voyage,
                                                                agence)));
        }
}