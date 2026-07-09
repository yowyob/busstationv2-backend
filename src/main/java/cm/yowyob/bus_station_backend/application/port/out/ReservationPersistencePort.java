package cm.yowyob.bus_station_backend.application.port.out;

import cm.yowyob.bus_station_backend.domain.model.Historique;
import cm.yowyob.bus_station_backend.domain.model.Passager;
import cm.yowyob.bus_station_backend.domain.model.Reservation;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

public interface ReservationPersistencePort {
    // --- Réservations ---
    Mono<Reservation> save(Reservation reservation);
    Flux<Reservation> findAll(Pageable pageable);
    Mono<Reservation> findById(UUID id);
    Flux<Reservation> findByUserId(UUID userId, Pageable pageable);
    Flux<Reservation> findByVoyageId(UUID voyageId);
    Flux<Reservation> findByAgenceId(UUID agenceId, Pageable pageable); // Nécessite une jointure ou filtrage dans l'infra
    Mono<Void> deleteById(UUID id);

    // --- Passagers ---
    Flux<Passager> savePassagers(List<Passager> passagers);
    Flux<Passager> findPassagersByReservationId(UUID reservationId);
    Mono<Passager> findPassagerById(UUID passagerId);
    Mono<Void> deletePassagersByReservationId(UUID reservationId);

    // Méthode critique pour la gestion de concurrence des places
    Mono<Boolean> decrementPlacesVoyage(UUID voyageId, int count);
    Mono<Void> incrementPlacesVoyage(UUID voyageId, int count); // En cas d'annulation/échec


    // Gestion Historique
    Mono<Historique> saveHistorique(Historique historique);
    Mono<Historique> findHistoriqueByReservationId(UUID reservationId);
    Flux<Historique> findHistoriqueByUserId(UUID userId);

    // Pour le scheduler
    Flux<Reservation> findPendingReservations();
    Flux<Integer> findConfirmedPassagersPlaces(UUID voyageId);
    Flux<Integer> findReservedPassagersPlaces(UUID voyageId);

    // Agrégations pour les statistiques
    Mono<Long> countReservationsByAgenceId(UUID agenceId);
    Mono<Long> countByUserId(UUID userId);
    Mono<Long> countAllReservations();
    Mono<Double> sumRevenusByAgenceId(UUID agenceId);
}