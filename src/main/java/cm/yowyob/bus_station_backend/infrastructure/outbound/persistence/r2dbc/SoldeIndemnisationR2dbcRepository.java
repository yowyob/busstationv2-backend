package cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.r2dbc;

import cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.entity.SoldeIndemnisationEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface SoldeIndemnisationR2dbcRepository extends R2dbcRepository<SoldeIndemnisationEntity, UUID> {

    // ---------- LOOKUP UNIQUE ----------

    @Query("""
                SELECT *
                FROM soldes_indemnisation
                WHERE id_user = :userId
                AND id_agence_voyage = :agenceId
            """)
    Mono<SoldeIndemnisationEntity> findByUserIdAndAgenceId(
            UUID userId,
            UUID agenceId);

    // ---------- USER ----------

    @Query("""
                SELECT *
                FROM soldes_indemnisation
                WHERE id_user = :userId
                LIMIT :#{#pageable.pageSize}
                OFFSET :#{#pageable.offset}
            """)
    Flux<SoldeIndemnisationEntity> findByUserIdPaged(UUID userId, Pageable pageable);

}
