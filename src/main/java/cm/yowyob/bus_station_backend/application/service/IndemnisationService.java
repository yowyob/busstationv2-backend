package cm.yowyob.bus_station_backend.application.service;

import cm.yowyob.bus_station_backend.application.port.in.IndemnisationUseCase;
import cm.yowyob.bus_station_backend.application.port.out.IndemnisationPersistencePort;
import cm.yowyob.bus_station_backend.application.port.out.ReservationPersistencePort;
import cm.yowyob.bus_station_backend.application.port.out.VoyagePersistencePort;
import cm.yowyob.bus_station_backend.domain.enums.StatutCoupon;
import cm.yowyob.bus_station_backend.domain.enums.StatutPayment;
import cm.yowyob.bus_station_backend.domain.exception.BusinessRuleViolationException;
import cm.yowyob.bus_station_backend.domain.exception.ResourceNotFoundException;
import cm.yowyob.bus_station_backend.domain.model.Coupon;
import cm.yowyob.bus_station_backend.domain.model.SoldeIndemnisation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class IndemnisationService implements IndemnisationUseCase {

    private final IndemnisationPersistencePort indemnisationPort;
    private final ReservationPersistencePort reservationPort;
    private final VoyagePersistencePort voyagePort;
    private final TransactionalOperator rxtx; // Pour garantir l'atomicité de l'application du coupon

    @Override
    public Flux<Coupon> getCouponsByUserId(UUID userId, Pageable pageable) {
        log.info("Récupération paginée des coupons user={},pageable={}", userId, pageable);
        return indemnisationPort.findCouponsByUserId(userId, pageable);
    }

    @Override
    public Flux<Coupon> getCouponsByUserIdAndAgenceId(UUID userId, UUID aganceId, Pageable pageable) {
        return indemnisationPort.findCouponsByUserIdAndAgenceId(userId, aganceId, pageable);
    }

    @Override
    public Mono<Coupon> getCouponById(UUID id) {
        return indemnisationPort.findCouponById(id)
                .switchIfEmpty(Mono.error(
                        new ResourceNotFoundException("Coupon introuvable")));
    }

    @Override
    public Mono<SoldeIndemnisation> getSoldeById(UUID id) {
        return indemnisationPort.findSoldeById(id)
                .switchIfEmpty(Mono.error(
                        new ResourceNotFoundException("Solde introuvable")));
    }

    @Override
    public Flux<SoldeIndemnisation> getAllSoldes() {
        // TODO : implementer plus tard si vraiment nécessaire
        return Flux.empty();
    }

    @Override
    public Flux<SoldeIndemnisation> getSoldesByUserId(UUID userId, Pageable pageable) {
        return indemnisationPort.findSoldesByUserId(userId, pageable);
    }

    @Override
    public Mono<SoldeIndemnisation> getSoldeByUserIdAndAgenceId(UUID userId, UUID agenceId) {
        return indemnisationPort.findSoldeByUserIdAndAgenceId(userId, agenceId)
                .switchIfEmpty(Mono.error(
                        new ResourceNotFoundException("Solde introuvable")));
    }

    /**
     * Applique un coupon à une réservation.
     * Logique métier :
     * 1. Vérifier si le coupon est valide (Statut et Date)
     * 2. Vérifier si le coupon appartient à l'utilisateur
     * 3. Vérifier si le coupon correspond à l'agence du voyage réservé
     * 4. Appliquer la réduction sur la réservation
     * 5. Marquer le coupon comme utilisé et mettre à jour le solde
     */
    @Override
    public Mono<Boolean> applyCouponToReservation(UUID couponId, UUID reservationId, UUID userId) {
        return Mono.zip(
                indemnisationPort.findCouponById(couponId),
                reservationPort.findById(reservationId))
                .flatMap(tuple -> {
                    Coupon coupon = tuple.getT1();
                    var reservation = tuple.getT2();

                    // Validations de base
                    if (coupon.getStatusCoupon() != StatutCoupon.VALIDE) {
                        return Mono.error(new BusinessRuleViolationException("Le coupon n'est plus valide"));
                    }
                    if (coupon.getDateFin().isBefore(LocalDateTime.now())) {
                        coupon.setStatusCoupon(StatutCoupon.EXPIRER);
                        return indemnisationPort.saveCoupon(coupon)
                                .then(Mono.error(new BusinessRuleViolationException("Le coupon a expiré")));
                    }

                    // Vérification de l'appartenance et de l'agence
                    return indemnisationPort.findSoldeById(coupon.getIdSoldeIndemnisation())
                            .flatMap(solde -> {
                                if (!solde.getIdUser().equals(userId)
                                        || !solde.getIdUser().equals(reservation.getIdUser())) {
                                    return Mono.error(
                                            new BusinessRuleViolationException("Ce coupon ne vous appartient pas"));
                                }

                                // Récupérer l'agence du voyage pour vérifier la correspondance
                                return voyagePort.findLigneVoyageByVoyageId(reservation.getIdVoyage())
                                        .flatMap(ligne -> {
                                            if (!ligne.getIdAgenceVoyage().equals(solde.getIdAgenceVoyage())) {
                                                return Mono.error(new BusinessRuleViolationException(
                                                        "Ce coupon n'est pas valable pour cette agence"));
                                            }

                                            // Calcul de l'application du coupon
                                            double montantAReduire = Math.min(
                                                    reservation.getPrixTotal() - reservation.getMontantPaye(),
                                                    coupon.getValeur());

                                            reservation.setMontantPaye(reservation.getMontantPaye() + montantAReduire);
                                            if (reservation.getMontantPaye() >= reservation.getPrixTotal()) {
                                                reservation.setStatutPayement(StatutPayment.PAID);
                                            }

                                            // Mise à jour du solde et du coupon
                                            solde.setSolde(solde.getSolde() - montantAReduire);
                                            coupon.setStatusCoupon(StatutCoupon.UTILISER);

                                            // Persistence transactionnelle
                                            return Mono.when(
                                                    reservationPort.save(reservation),
                                                    indemnisationPort.saveSolde(solde),
                                                    indemnisationPort.saveCoupon(coupon)).thenReturn(true);
                                        });
                            });
                })
                .as(rxtx::transactional) // Saga local : Tout ou rien
                .onErrorResume(e -> {
                    log.error("Erreur lors de l'application du coupon : {}", e.getMessage());
                    return Mono.just(false);
                });
    }
}
