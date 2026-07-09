package cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.r2dbc;

import cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.entity.AgenceVoyageEntity;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface AgenceVoyageR2dbcRepository extends R2dbcRepository<AgenceVoyageEntity, UUID> {

    Mono<AgenceVoyageEntity> findByUserId(UUID userId);

    Flux<AgenceVoyageEntity> findByOrganisationId(UUID organisationId);

    Flux<AgenceVoyageEntity> findByGareRoutiereId(UUID gareRoutiereId);

    Mono<Boolean> existsByLongName(String longName);

    Mono<Boolean> existsByShortName(String shortName);
}