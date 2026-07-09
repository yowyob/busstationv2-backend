package cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.r2dbc;

import cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.entity.VoyageBrouillonEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface VoyageBrouillonR2dbcRepository extends ReactiveCrudRepository<VoyageBrouillonEntity, UUID> {

    @Query("""
        SELECT * FROM voyages_brouillon
        WHERE agence_voyage_id = :agenceId
          AND (CAST(:statut AS VARCHAR) IS NULL OR statut_brouillon = :statut)
        ORDER BY created_at DESC
        """)
    Flux<VoyageBrouillonEntity> findByAgence(UUID agenceId, String statut);

    Mono<Long> countByAgenceVoyageId(UUID agenceVoyageId);
}