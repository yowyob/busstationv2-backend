package cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.r2dbc;

import cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.entity.TauxPeriodeEntity;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface TauxPeriodeR2dbcRepository extends R2dbcRepository<TauxPeriodeEntity, UUID> {
    Flux<TauxPeriodeEntity> findByIdPolitiqueAnnulation(UUID idPolitique);

    Mono<Void> deleteByIdPolitiqueAnnulation(UUID idPolitique);
}
