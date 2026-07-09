package cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.r2dbc;

import cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.entity.VehiculeEntity;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface VehiculeR2dbcRepository extends R2dbcRepository<VehiculeEntity, UUID> {

    Flux<VehiculeEntity> findByIdAgenceVoyage(UUID agenceId);
}
