package cm.yowyob.bus_station_backend.application.strategy.reservation;

import cm.yowyob.bus_station_backend.application.port.out.ReservationPersistencePort;
import cm.yowyob.bus_station_backend.application.port.out.VoyagePersistencePort;
import cm.yowyob.bus_station_backend.domain.enums.StatutReservation;
import cm.yowyob.bus_station_backend.domain.model.Reservation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

//@Component
@RequiredArgsConstructor
@Slf4j
public class ReservationSagaOrchestrator {

    private final VoyagePersistencePort voyagePort;
    private final ReservationPersistencePort reservationPersistencePort;

    /**
     * Étape 1 SAGA : Création de la réservation impliquant le verrouillage des places.
     * Si le verrouillage échoue, la réservation est annulée (Compensation).

    @Transactional
    public void processReservationCreation(Reservation reservation, List<Integer> placeNumbers) {
        log.info("SAGA: Début transaction pour réservation {}", reservation.getIdReservation());

        try {
            // 1. Tenter de verrouiller les places dans le contexte Voyage
            boolean seatsLocked = voyagePort.tryLockSeats(
                    reservation.getIdVoyage(),
                    reservation.getNbrPassager(),
                    placeNumbers
            );

            if (!seatsLocked) {
                throw new RuntimeException("Impossible de verrouiller les places. Annulation SAGA.");
            }

            // 2. Si succès, on garde le statut RESERVER (ou PENDING_PAYMENT)
            log.info("SAGA: Places verrouillées avec succès. Réservation validée.");

        } catch (Exception e) {
            log.error("SAGA: Erreur lors de la création. Exécution de la compensation.", e);
            compensateReservationCreation(reservation);
            throw e; // Relancer pour informer le contrôleur
        }
    }
     */


    /**
     * Compensation : Annuler la réservation et libérer les places si nécessaire.

    private void compensateReservationCreation(Reservation reservation) {
        reservation.setStatutReservation(StatutReservation.ANNULER);
        reservationPersistencePort.save(reservation);
        // Note: Si le verrouillage avait réussi partiellement, il faudrait appeler voyagePort.releaseSeats
    }
     */

    /**
     * Étape 2 SAGA : Confirmation après paiement.

    @Transactional
    public void processReservationConfirmation(Reservation reservation) {
        try {
            // Confirmer définitivement les places (décrémenter stock restant, incrémenter confirmés)
            voyagePort.confirmSeats(reservation.getIdVoyage(), reservation.getNbrPassager());

            reservation.setStatutReservation(StatutReservation.CONFIRMER);
            reservationPersistencePort.save(reservation);

        } catch (Exception e) {
            log.error("SAGA: Erreur lors de la confirmation. Compensation requise.", e);
            // Logique de remboursement éventuel ou mise en statut ECHEC_CONFIRMATION
            throw e;
        }
    }
     */
}