package cm.yowyob.bus_station_backend.application.port.in;

import cm.yowyob.bus_station_backend.application.dto.reservation.ReservationCancelByAgenceDTO;
import cm.yowyob.bus_station_backend.application.dto.reservation.ReservationCancelDTO;
import cm.yowyob.bus_station_backend.application.dto.voyage.VoyageCancelDTO;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface AnnulationUseCase {
    /**
     * Annule un voyage complet (Action Agence).
     * @return Le montant du risque financier ou impact.
     */
    Mono<Double> cancelVoyage(VoyageCancelDTO cancelDTO, UUID userId);

    /**
     * Annule une réservation par le client.
     * @return Le montant des frais ou du remboursement (-1 si remboursé via coupon).
     */
    Mono<Double> cancelReservationByUser(ReservationCancelDTO cancelDTO, UUID userId);

    /**
     * Annule une réservation spécifique par l'agence.
     */
    Mono<Double> cancelReservationByAgence(ReservationCancelByAgenceDTO cancelDTO, UUID userId);

    /**
     * Vérifie et traite les réservations expirées (Scheduled Task logic).
     */
    Mono<Void> processExpiredReservations();


    /**
     * Vérifie si un utilisateur a le droit d'annuler/gérer un voyage (Logique métier pure).
     */
    Mono<Boolean> isAuthorizedToManageTravel(UUID userId, UUID voyageId);
}
