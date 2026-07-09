package cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.r2dbc;

import cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.entity.CouponEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface CouponR2dbcRepository extends R2dbcRepository<CouponEntity, UUID> {

    // ---------- USER ----------

    @Query("""
        SELECT *
        FROM coupons
        WHERE user_id = :userId
        ORDER BY created_at DESC
        LIMIT :#{#pageable.pageSize}
        OFFSET :#{#pageable.offset}
    """)
    Flux<CouponEntity> findByUserIdPaged(UUID userId, Pageable pageable);

    // ---------- USER + AGENCE ----------

    @Query("""
        SELECT *
        FROM coupons
        WHERE user_id = :userId
        AND id_agence_voyage = :agenceId
        ORDER BY created_at DESC
        LIMIT :#{#pageable.pageSize}
        OFFSET :#{#pageable.offset}
    """)
    Flux<CouponEntity> findByUserIdAndAgenceIdPaged(UUID userId, UUID agenceId, Pageable pageable);
}
