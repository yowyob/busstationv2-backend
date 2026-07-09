package cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.r2dbc;

import cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.entity.GareRoutiereEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface GareRoutiereR2dbcRepository extends ReactiveCrudRepository<GareRoutiereEntity, UUID> {

    Flux<GareRoutiereEntity> findByManagerId(UUID managerId);
}
