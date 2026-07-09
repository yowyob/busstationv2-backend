package cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.repository;

import java.util.UUID;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.entity.AffiliationAgenceVoyageEntity;
import reactor.core.publisher.Flux;

public interface AffiliationAgenceVoyageR2dbcRepository
        extends ReactiveCrudRepository<AffiliationAgenceVoyageEntity, UUID> {

    @Query("SELECT * FROM affiliation_agence_voyage WHERE gare_routiere_id = :gareRoutiereId ORDER BY created_at DESC")
    Flux<AffiliationAgenceVoyageEntity> findByGareRoutiereId(UUID gareRoutiereId);

    // --- LOT 9 ---
    @Query("SELECT * FROM affiliation_agence_voyage WHERE agency_id = :agencyId ORDER BY created_at DESC")
    Flux<AffiliationAgenceVoyageEntity> findByAgencyId(UUID agencyId);
}
