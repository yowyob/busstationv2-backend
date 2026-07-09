package cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.r2dbc;

import cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.entity.ChauffeurEntity;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface ChauffeurR2dbcRepository extends R2dbcRepository<ChauffeurEntity, UUID> {

    Mono<ChauffeurEntity> findByUserId(@Param("userId") UUID userId);

    Flux<ChauffeurEntity> findByAgenceVoyageId(@Param("agenceId") UUID agenceId);
}
