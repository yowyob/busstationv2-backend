package cm.yowyob.bus_station_backend.application.service;

import cm.yowyob.bus_station_backend.application.dto.reservation.ReservationCancelByAgenceDTO;
import cm.yowyob.bus_station_backend.application.dto.reservation.ReservationCancelDTO;
import cm.yowyob.bus_station_backend.application.dto.voyage.VoyageCancelDTO;
import cm.yowyob.bus_station_backend.application.port.in.AnnulationUseCase;
import cm.yowyob.bus_station_backend.application.port.out.*;
import cm.yowyob.bus_station_backend.domain.enums.*;
import cm.yowyob.bus_station_backend.domain.exception.AnnulationException;
import cm.yowyob.bus_station_backend.domain.exception.ResourceNotFoundException;
import cm.yowyob.bus_station_backend.domain.exception.UnauthorizeException;
import cm.yowyob.bus_station_backend.domain.factory.NotificationFactory;
import cm.yowyob.bus_station_backend.domain.model.*;
import cm.yowyob.bus_station_backend.domain.util.AnnulationOperator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnnulationService implements AnnulationUseCase {

    private final ReservationPersistencePort reservationPort;
    private final OrganizationPersistencePort organizationPort;
    private final VoyagePersistencePort voyagePort;
    private final AgencePersistencePort agencePort;
    private final UserPersistencePort userPort;
    private final IndemnisationPersistencePort indemnisationPort;
    private final NotificationPort notificationPort;
    private final TransactionalOperator rxtx;

    @Override
    public Mono<Double> cancelVoyage(VoyageCancelDTO cancelDTO, UUID userId) {
        return isAuthorizedToManageTravel(userId, cancelDTO.getIdVoyage())
                .flatMap(isAuth -> {
                    if (!isAuth)
                        return Mono.error(new AnnulationException("Non autorisé"));
                    return voyagePort.findById(cancelDTO.getIdVoyage());
                })
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Voyage non trouvé")))
                .flatMap(voyage -> reservationPort.findByVoyageId(voyage.getIdVoyage()).collectList()
                        .flatMap(reservations -> {
                            // Saga d'annulation pour chaque réservation
                            // Utiliser concatMap pour éviter les collisions sur l'objet Voyage
                            return Flux.fromIterable(reservations)
                                    .concatMap(res -> {
                                        ReservationCancelByAgenceDTO resDto = new ReservationCancelByAgenceDTO();
                                        resDto.setIdReservation(res.getIdReservation());
                                        resDto.setCanceled(cancelDTO.isCanceled());
                                        resDto.setCauseAnnulation(cancelDTO.getCauseAnnulation());
                                        resDto.setOrigineAnnulation(cancelDTO.getOrigineAnnulation());
                                        return cancelReservationByAgence(resDto, userId);
                                    })
                                    .reduce(0.0, (acc, risk) -> acc + (risk > 0 ? risk : 0))
                                    .flatMap(totalRisk -> {
                                        if (cancelDTO.isCanceled()) {
                                            // RE-FETCH le voyage pour avoir les compteurs à jour après les annulations de réservations
                                            return voyagePort.findById(voyage.getIdVoyage())
                                                    .flatMap(v -> {
                                                        v.setStatusVoyage(StatutVoyage.ANNULE);
                                                        return voyagePort.save(v);
                                                    })
                                                    .then(notificationPort.sendNotification(
                                                            NotificationFactory.createVoyageCancelledEvent(voyage,
                                                                    cancelDTO.getAgenceVoyageId(),
                                                                    RecipientType.AGENCY)))
                                                    .thenReturn(totalRisk);
                                        }
                                        return Mono.just(totalRisk);
                                    });
                        }))
                .as(rxtx::transactional);
    }

    @Override
    public Mono<Double> cancelReservationByUser(ReservationCancelDTO cancelDTO, UUID userId) {
        LocalDateTime now = LocalDateTime.now();

        return reservationPort.findById(cancelDTO.getIdReservation())
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Réservation inexistante")))
                .flatMap(res -> {
                    if (!res.getIdUser().equals(userId))
                        return Mono.error(new UnauthorizeException(
                                "Propriétaire invalide, vous n'êtes pas authorisé à annuler cette réservation"));
                    if (res.getStatutReservation() == StatutReservation.ANNULER)
                        return Mono.error(new AnnulationException("Cette réservation est déjà annulée"));

                    return getCancellationData(res.getIdVoyage())
                            .flatMap(data -> {
                                double taux = AnnulationOperator.tauxannulation(
                                        data.classVoyage, data.politique,
                                        data.voyage.getDateLimiteReservation(),
                                        data.voyage.getDateLimiteConfirmation(),
                                        now);

                                if (!cancelDTO.isCanceled())
                                    return Mono.just(taux);

                                return executeUserCancellationSaga(res, cancelDTO, data, taux, now);
                            });
                });
    }

    private Mono<Double> executeUserCancellationSaga(Reservation res, ReservationCancelDTO dto,
            CancellationData data, double taux, LocalDateTime now) {

        return reservationPort.findPassagersByReservationId(res.getIdReservation())
                .filter(p -> Arrays.asList(dto.getIdPassagers()).contains(p.getIdPassager()))
                .collectList()
                .flatMap(passagersToCancel -> {
                    int count = passagersToCancel.size();
                    double montantSubstitut = calculateSubstitut(res, count, data.classVoyage.getPrix());

                    // Mise à jour domaine
                    data.voyage.libererPlaces(count, res.getStatutReservation() == StatutReservation.CONFIRMER);
                    
                    res.setNbrPassager(res.getNbrPassager() - count);
                    res.setMontantPaye(Math.max(0, res.getMontantPaye() - (count * data.classVoyage.getPrix())));
                    res.setPrixTotal(res.getNbrPassager() * data.classVoyage.getPrix());
                    if (res.getNbrPassager() <= 0)
                        res.setStatutReservation(StatutReservation.ANNULER);

                    Historique hist = createHistorique(res, dto.getCauseAnnulation(), dto.getOrigineAnnulation(), taux,
                            now);

                    return Mono.when(
                            reservationPort.save(res),
                            voyagePort.save(data.voyage),
                            reservationPort.saveHistorique(hist),
                            Flux.fromIterable(passagersToCancel)
                                    .flatMap(p -> reservationPort.deletePassagersByReservationId(p.getIdPassager()))
                                    .then(),
                            processIndemnisation(res.getIdUser(), data.ligne.getIdAgenceVoyage(), montantSubstitut,
                                    taux, data.politique, hist.getIdHistorique(), now))
                            .then(notificationPort
                                    .sendNotification(NotificationFactory.createReservationCancelledEvent(res)))
                            .thenReturn(-1.0);
                })
                .as(rxtx::transactional);
    }

    @Override
    public Mono<Double> cancelReservationByAgence(ReservationCancelByAgenceDTO cancelDTO, UUID userId) {
        LocalDateTime now = LocalDateTime.now();

        return reservationPort.findById(cancelDTO.getIdReservation())
                .flatMap(res -> isAuthorizedToManageTravel(userId, res.getIdVoyage())
                        .flatMap(isAuth -> {
                            if (!isAuth)
                                return Mono.error(new AnnulationException("Non autorisé"));
                            return getCancellationData(res.getIdVoyage()).flatMap(data -> {
                                double tauxComp = AnnulationOperator.tauxCompensation(
                                        data.classVoyage, data.politique,
                                        data.voyage.getDateLimiteReservation(),
                                        data.voyage.getDateLimiteConfirmation(),
                                        now);

                                if (!cancelDTO.isCanceled())
                                    return Mono.just(tauxComp * res.getMontantPaye());

                                // Saga Annulation Agence
                                data.voyage.libererPlaces(res.getNbrPassager(), res.getStatutReservation() == StatutReservation.CONFIRMER);
                                res.setStatutReservation(StatutReservation.ANNULER);

                                Historique hist = createHistorique(res, cancelDTO.getCauseAnnulation(),
                                        cancelDTO.getOrigineAnnulation(), 0, now);
                                hist.setCompensation(tauxComp);

                                return Mono.when(
                                        reservationPort.save(res),
                                        voyagePort.save(data.voyage),
                                        reservationPort.saveHistorique(hist),
                                        reservationPort.deletePassagersByReservationId(res.getIdReservation()),
                                        processIndemnisation(res.getIdUser(), data.ligne.getIdAgenceVoyage(),
                                                res.getMontantPaye(), -tauxComp, data.politique, hist.getIdHistorique(),
                                                now))
                                        .then(notificationPort.sendNotification(NotificationFactory
                                                .createReservationCancelledByAgencyEvent(res, cancelDTO)))
                                        .thenReturn(-1.0);
                            });
                        }))
                .as(rxtx::transactional);
    }

    @Override
    public Mono<Void> processExpiredReservations() {
        LocalDateTime now = LocalDateTime.now();

        return reservationPort.findPendingReservations()
                .filter(res -> res.getStatutReservation() == StatutReservation.RESERVER ||
                        res.getStatutReservation() == StatutReservation.VALIDER)
                .flatMap(res -> voyagePort.findById(res.getIdVoyage())
                        .filter(v -> {
                            LocalDateTime dateLimiteConfirmation = v.getDateLimiteConfirmation();
                            return dateLimiteConfirmation != null &&
                                    now.isAfter(dateLimiteConfirmation);
                        })
                        .flatMap(v -> {
                            ReservationCancelByAgenceDTO dto = new ReservationCancelByAgenceDTO();
                            dto.setIdReservation(res.getIdReservation());
                            dto.setCanceled(true);
                            dto.setCauseAnnulation("Expiration du délai de confirmation");
                            dto.setOrigineAnnulation("Système");
                            return cancelReservationByAgence(dto, res.getIdUser()).then();
                        }))
                .then();
    }

    @Override
    public Mono<Boolean> isAuthorizedToManageTravel(UUID userId, UUID voyageId) {
        return userPort.findById(userId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Utilisateur introuvable")))
                .flatMap(user -> {
                    List<RoleType> roles = user.getRoles();
                    if (roles == null) return Mono.just(false);

                    // Platform ADMIN can manage anything
                    if (roles.contains(RoleType.ADMIN)) {
                        return Mono.just(true);
                    }

                    if (!roles.contains(RoleType.AGENCE_VOYAGE) &&
                            !roles.contains(RoleType.ORGANISATION)) {
                        return Mono.just(false);
                    }

                    return voyagePort.findLigneVoyageByVoyageId(voyageId)
                            .filter(ligne -> ligne.getIdAgenceVoyage() != null)
                            .flatMap(ligne -> agencePort.findById(ligne.getIdAgenceVoyage()))
                            .flatMap(agence -> {
                                // Vérification pour AGENCE_VOYAGE
                                if (roles.contains(RoleType.AGENCE_VOYAGE) &&
                                        agence.getUserId() != null &&
                                        agence.getUserId().equals(userId)) {
                                    return Mono.just(true);
                                }

                                // Vérification pour ORGANISATION
                                if (roles.contains(RoleType.ORGANISATION) &&
                                        agence.getOrganisationId() != null) {
                                    return organizationPort.findById(agence.getOrganisationId())
                                            .map(org -> org.getOrganizationId() != null &&
                                                    org.getOrganizationId().equals(userId))
                                            .defaultIfEmpty(false);
                                }

                                return Mono.just(false);
                            })
                            .defaultIfEmpty(false);
                })
                .onErrorResume(e -> {
                    log.error("Erreur lors de la vérification des autorisations: ", e);
                    return Mono.just(false);
                });
    }

    // --- Helpers Réactifs ---

    private Mono<CancellationData> getCancellationData(UUID voyageId) {
        return voyagePort.findById(voyageId)
                .flatMap(v -> voyagePort.findLigneVoyageByVoyageId(voyageId)
                        .flatMap(l -> Mono.zip(
                                voyagePort.findClassVoyageById(l.getIdClassVoyage()),
                                agencePort.findPolitiqueByAgenceId(l.getIdAgenceVoyage()))
                                .map(t -> new CancellationData(v, l, t.getT1(), t.getT2()))));
    }

    private double calculateSubstitut(Reservation res, int count, double unitPrice) {
        double currentPaye = res.getMontantPaye();
        double potential = count * unitPrice;
        return Math.min(currentPaye, potential);
    }

    private Mono<Void> processIndemnisation(UUID userId, UUID agenceId, double montant, double taux,
            PolitiqueAnnulation pol, UUID histId, LocalDateTime now) {
        if (montant <= 0)
            return Mono.empty();

        double valeurCoupon = montant * (1 - taux);

        return indemnisationPort.findSoldeByUserIdAndAgenceId(userId, agenceId)
                .switchIfEmpty(indemnisationPort.saveSolde(SoldeIndemnisation.builder()
                        .idSolde(UUID.randomUUID()).idUser(userId).idAgenceVoyage(agenceId).solde(0.0).build()))
                .flatMap(solde -> {
                    solde.setSolde(solde.getSolde() + valeurCoupon);
                    Coupon coupon = Coupon.builder()
                            .idCoupon(UUID.randomUUID())
                            .dateDebut(now)
                            .dateFin(now.plus(pol.getDureeCoupon()))
                            .valeur(valeurCoupon)
                            .statusCoupon(StatutCoupon.VALIDE)
                            .idHistorique(histId)
                            .idSoldeIndemnisation(solde.getIdSolde())
                            .build();
                    return Mono.when(indemnisationPort.saveSolde(solde), indemnisationPort.saveCoupon(coupon));
                });
    }

    private Historique createHistorique(Reservation res, String cause, String origine, double taux, LocalDateTime now) {
        StatutHistorique stat = res.getStatutReservation() == StatutReservation.CONFIRMER
                ? StatutHistorique.ANNULER_PAR_USAGER_APRES_CONFIRMATION
                : StatutHistorique.ANNULER_PAR_USAGER_APRES_RESERVATION;

        return Historique.builder()
                .idHistorique(UUID.randomUUID())
                .idReservation(res.getIdReservation())
                .dateAnnulation(now)
                .causeAnnulation(cause)
                .origineAnnulation(origine)
                .tauxAnnulation(taux)
                .statusHistorique(stat)
                .build();
    }

    // Classe interne pour grouper les données de calcul (Data Transfer Object
    // interne)
    private record CancellationData(Voyage voyage, LigneVoyage ligne, ClassVoyage classVoyage,
            PolitiqueAnnulation politique) {
    }
}
