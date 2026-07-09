package cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.r2dbc;

import cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.entity.CreneauPlanningEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface CreneauPlanningR2dbcRepository extends ReactiveCrudRepository<CreneauPlanningEntity, UUID> {

    @Query("SELECT * FROM creneaux_planning WHERE id_planning = :planningId ORDER BY heure_depart ASC")
    Flux<CreneauPlanningEntity> findByIdPlanning(UUID planningId);

    @Query("SELECT * FROM creneaux_planning WHERE id_planning = :planningId AND actif = true ORDER BY heure_depart ASC")
    Flux<CreneauPlanningEntity> findActifsByIdPlanning(UUID planningId);

    @Query("DELETE FROM creneaux_planning WHERE id_planning = :planningId")
    Mono<Void> deleteByIdPlanning(UUID planningId);
}
