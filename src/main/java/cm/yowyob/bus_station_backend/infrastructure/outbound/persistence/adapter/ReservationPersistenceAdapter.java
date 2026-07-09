package cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.adapter;

import cm.yowyob.bus_station_backend.application.port.out.ReservationPersistencePort;
import cm.yowyob.bus_station_backend.domain.model.Historique;
import cm.yowyob.bus_station_backend.domain.model.Passager;
import cm.yowyob.bus_station_backend.domain.model.Reservation;
import cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.entity.HistoriqueEntity;
import cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.entity.PassagerEntity;
import cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.entity.ReservationEntity;
import cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.mapper.HistoriquePersistenceMapper;
import cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.mapper.PassagerPersistenceMapper;
import cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.mapper.ReservationPersistenceMapper;
import cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.r2dbc.HistoriqueR2dbcRepository;
import cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.r2dbc.PassagerR2dbcRepository;
import cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.r2dbc.ReservationR2dbcRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ReservationPersistenceAdapter implements ReservationPersistencePort {

    private final ReservationR2dbcRepository reservationRepository;
    private final PassagerR2dbcRepository passagerRepository;
    private final HistoriqueR2dbcRepository historiqueRepository;

    private final ReservationPersistenceMapper reservationMapper;
    private final PassagerPersistenceMapper passagerMapper;
    private final HistoriquePersistenceMapper historiqueMapper;

    // ------------------ RESERVATION ------------------

    @Override
    public Mono<Reservation> save(Reservation reservation) {
        ReservationEntity entity = reservationMapper.toEntity(reservation);
        if (entity.getIdReservation() == null) {
            entity.setIdReservation(UUID.randomUUID());
            entity.setAsNew();
            return reservationRepository.save(entity).map(reservationMapper::toDomain);
        }
        
        return reservationRepository.existsById(entity.getIdReservation())
                .flatMap(exists -> {
                    if (!exists) {
                        entity.setAsNew();
                    }
                    return reservationRepository.save(entity);
                })
                .map(reservationMapper::toDomain);
    }

    @Override
    public Flux<Reservation> findAll(Pageable pageable) {
        return reservationRepository.findAllPaged(pageable)
                .map(reservationMapper::toDomain);
    }

    @Override
    public Mono<Reservation> findById(UUID id) {
        return reservationRepository.findById(id)
                .map(reservationMapper::toDomain);
    }

    @Override
    public Flux<Reservation> findByUserId(UUID userId, Pageable pageable) {
        return reservationRepository.findByUserIdPaged(userId, pageable)
                .map(reservationMapper::toDomain);
    }

    @Override
    public Flux<Reservation> findByVoyageId(UUID voyageId) {
        return reservationRepository.findByIdVoyage(voyageId)
                .map(reservationMapper::toDomain);
    }

    /**
     * Jointure RESERVATION -> VOYAGE pour filtrer par agence
     */
    @Override
    public Flux<Reservation> findByAgenceId(UUID agenceId, Pageable pageable) {
        return reservationRepository.findByAgenceIdPaged(agenceId, pageable)
                .map(reservationMapper::toDomain);
    }

    @Override
    public Mono<Void> deleteById(UUID id) {
        return reservationRepository.deleteById(id);
    }

    // ------------------ PASSAGERS ------------------

    @Override
    public Flux<Passager> savePassagers(List<Passager> passagers) {
        List<PassagerEntity> entities = passagers.stream()
                .map(passagerMapper::toEntity)
                .map(entity -> {
                    if (entity.getIdPassager() == null) {
                        entity.setIdPassager(UUID.randomUUID());
                    }
                    entity.setAsNew(); // toujours INSERT
                    return entity;
                })
                .toList();
        return passagerRepository.saveAll(entities)
                .map(passagerMapper::toDomain);
    }

    @Override
    public Flux<Passager> findPassagersByReservationId(UUID reservationId) {
        return passagerRepository.findByIdReservation(reservationId)
                .map(passagerMapper::toDomain);
    }

    @Override
    public Mono<Passager> findPassagerById(UUID passagerId) {
        return passagerRepository.findById(passagerId)
                .map(passagerMapper::toDomain);
    }

    @Override
    public Mono<Void> deletePassagersByReservationId(UUID reservationId) {
        return passagerRepository.deleteByIdReservation(reservationId);
    }

    // ------------------ CONCURRENCY PLACES ------------------

    /**
     * UPDATE atomique → retourne true si assez de places
     */
    @Override
    public Mono<Boolean> decrementPlacesVoyage(UUID voyageId, int count) {
        return reservationRepository.decrementPlaces(voyageId, count)
                .map(updatedRows -> updatedRows > 0);
    }

    @Override
    public Mono<Void> incrementPlacesVoyage(UUID voyageId, int count) {
        return reservationRepository.incrementPlaces(voyageId, count).then();
    }

    // ------------------ HISTORIQUE ------------------

    @Override
    public Mono<Historique> saveHistorique(Historique historique) {
        HistoriqueEntity entity = historiqueMapper.toEntity(historique);
        if (historique.getIdHistorique() == null) {
            entity.setIdHistorique(UUID.randomUUID());
        }
        entity.setAsNew(); // toujours INSERT
        return historiqueRepository
                .save(entity)
                .map(historiqueMapper::toDomain);
    }

    @Override
    public Mono<Historique> findHistoriqueByReservationId(UUID reservationId) {
        return historiqueRepository.findByIdReservation(reservationId)
                .map(historiqueMapper::toDomain);
    }

    @Override
    public Flux<Historique> findHistoriqueByUserId(UUID userId) {
        return reservationRepository.findByIdUser(userId)
                .flatMap(reservation -> historiqueRepository.findByIdReservation(reservation.getIdReservation()))
                .map(historiqueMapper::toDomain);
    }

    // ------------------ SCHEDULER ------------------

    @Override
    public Flux<Reservation> findPendingReservations() {
        return reservationRepository.findPendingReservations(LocalDateTime.now())
                .map(reservationMapper::toDomain);
    }

    @Override
    public Flux<Integer> findConfirmedPassagersPlaces(UUID voyageId) {
        return reservationRepository.findConfirmedPassagersPlaces(voyageId);
    }

    @Override
    public Flux<Integer> findReservedPassagersPlaces(UUID voyageId) {
        return reservationRepository.findReservedPassagersPlaces(voyageId);
    }

    // ------------------ STATS ------------------

    @Override
    public Mono<Long> countReservationsByAgenceId(UUID agenceId) {
        return reservationRepository.countByAgenceId(agenceId);
    }

    @Override
    public Mono<Long> countByUserId(UUID userId) {
        return reservationRepository.countByUserId(userId);
    }

    @Override
    public Mono<Long> countAllReservations() {
        return reservationRepository.count();
    }

    @Override
    public Mono<Double> sumRevenusByAgenceId(UUID agenceId) {
        return reservationRepository.sumRevenusByAgenceId(agenceId);
    }
}
