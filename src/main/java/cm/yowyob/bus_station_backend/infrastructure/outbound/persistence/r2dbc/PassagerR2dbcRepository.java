package cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.r2dbc;

import cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.entity.PassagerEntity;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface PassagerR2dbcRepository extends R2dbcRepository<PassagerEntity, UUID> {
    Flux<PassagerEntity> findByIdReservation(UUID idReservation);

    Mono<Void> deleteByIdReservation(UUID idReservation);
}
