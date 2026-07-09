package cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.r2dbc;

import cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.entity.ReservationEntity;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.UUID;

public interface ReservationR2dbcRepository extends R2dbcRepository<ReservationEntity, UUID> {

    // ---------- PAGINATION ----------

    @Query("""
                SELECT *
                FROM reservations
                ORDER BY date_reservation DESC
                LIMIT :#{#pageable.pageSize}
                OFFSET :#{#pageable.offset}
            """)
    Flux<ReservationEntity> findAllPaged(Pageable pageable);

    @Query("""
                SELECT *
                FROM reservations
                WHERE id_user = :userId
                ORDER BY date_reservation DESC
                LIMIT :#{#pageable.pageSize}
                OFFSET :#{#pageable.offset}
            """)
    Flux<ReservationEntity> findByUserIdPaged(UUID userId, Pageable pageable);

    Flux<ReservationEntity> findByIdUser(UUID userId);

    Flux<ReservationEntity> findByIdVoyage(UUID voyageId);

    // ---------- AGENCE (JOIN) ----------

    @Query("""
                SELECT r.*
                FROM reservations r
                JOIN voyages v ON v.id_voyage = r.id_voyage
                JOIN lignes_voyage l ON l.id_voyage = v.id_voyage
                WHERE l.id_agence_voyage = :agenceId
                ORDER BY r.date_reservation DESC
                LIMIT :#{#pageable.pageSize}
                OFFSET :#{#pageable.offset}
            """)
    Flux<ReservationEntity> findByAgenceIdPaged(UUID agenceId, Pageable pageable);

    // ---------- CONCURRENCY ----------

    @Modifying
    @Query("UPDATE voyages SET nbr_place_reservable = nbr_place_reservable - :count WHERE id_voyage = :voyageId AND nbr_place_reservable >= :count")
    Mono<Integer> decrementPlaces(@Param("voyageId") UUID voyageId, @Param("count") int count);

    @Modifying
    @Query("UPDATE voyages SET nbr_place_reservable = nbr_place_reservable + :count WHERE id_voyage = :voyageId")
    Mono<Integer> incrementPlaces(@Param("voyageId") UUID voyageId, @Param("count") int count);

    // ---------- SCHEDULER ----------

    @Query("""
                SELECT *
                FROM reservations
                WHERE statut_reservation = 'PENDING'
                AND date_reservation < :now
            """)
    Flux<ReservationEntity> findPendingReservations(LocalDateTime now);

    // ---------- PLACES ----------

    @Query("""
                SELECT place_choisis
                FROM passagers
                WHERE id_voyage = :voyageId
                AND statut = 'CONFIRMED'
            """)
    Flux<Integer> findConfirmedPassagersPlaces(UUID voyageId);

    @Query("""
                SELECT place_choisis
                FROM passagers
                WHERE id_voyage = :voyageId
                AND statut = 'RESERVED'
            """)
    Flux<Integer> findReservedPassagersPlaces(UUID voyageId);

    // ---------- STATS ----------

    @Query("SELECT COUNT(*) FROM reservations WHERE id_user = :userId")
    Mono<Long> countByUserId(UUID userId);

    @Query("""
                SELECT COUNT(*)
                FROM reservations r
                JOIN voyages v ON v.id_voyage = r.id_voyage
                JOIN lignes_voyage l ON l.id_voyage = v.id_voyage
                WHERE l.id_agence_voyage = :agenceId
            """)
    Mono<Long> countByAgenceId(UUID agenceId);

    @Query("""
                SELECT COALESCE(SUM(r.prix_total), 0)
                FROM reservations r
                JOIN voyages v ON v.id_voyage = r.id_voyage
                JOIN lignes_voyage l ON l.id_voyage = v.id_voyage
                WHERE l.id_agence_voyage = :agenceId
                AND r.statut_reservation = 'CONFIRMER'
            """)
    Mono<Double> sumRevenusByAgenceId(UUID agenceId);
}
