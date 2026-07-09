package cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.r2dbc;

import cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.entity.PlanningVoyageEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.util.UUID;

@Repository
public interface PlanningVoyageR2dbcRepository extends ReactiveCrudRepository<PlanningVoyageEntity, UUID> {

    @Query("SELECT * FROM plannings_voyage WHERE id_agence_voyage = :agenceId ORDER BY date_creation DESC")
    Flux<PlanningVoyageEntity> findByIdAgenceVoyage(UUID agenceId);

    @Query("SELECT * FROM plannings_voyage WHERE id_agence_voyage = :agenceId AND statut = 'ACTIF' ORDER BY date_creation DESC")
    Flux<PlanningVoyageEntity> findActifsByIdAgenceVoyage(UUID agenceId);
}
