package cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.r2dbc;

import cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.entity.AlerteAgenceEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface AlerteR2dbcRepository extends ReactiveCrudRepository<AlerteAgenceEntity, UUID> {

    @Query("SELECT * FROM alertes_agence WHERE gare_id = :gareId ORDER BY created_at DESC")
    Flux<AlerteAgenceEntity> findByGareId(UUID gareId);

    @Query("SELECT * FROM alertes_agence WHERE agence_id = :agenceId ORDER BY created_at DESC")
    Flux<AlerteAgenceEntity> findByAgenceId(UUID agenceId);
}