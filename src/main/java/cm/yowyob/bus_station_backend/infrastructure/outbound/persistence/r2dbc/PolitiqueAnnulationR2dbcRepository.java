package cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.r2dbc;

import cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.entity.PolitiqueAnnulationEntity;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface PolitiqueAnnulationR2dbcRepository extends R2dbcRepository<PolitiqueAnnulationEntity, UUID> {
    Mono<PolitiqueAnnulationEntity>  findByIdAgenceVoyage(UUID agenceVoyageId);
}
