package cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.r2dbc;

import cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.entity.LigneVoyageEntity;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface LigneVoyageR2dbcRepository extends R2dbcRepository<LigneVoyageEntity, UUID> {

    Mono<LigneVoyageEntity> findByIdVoyage(UUID voyageId);

    Flux<LigneVoyageEntity> findByIdAgenceVoyage(UUID agenceId);
}
