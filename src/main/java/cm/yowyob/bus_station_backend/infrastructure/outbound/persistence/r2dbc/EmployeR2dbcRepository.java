package cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.r2dbc;

import cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.entity.EmployeEntity;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface EmployeR2dbcRepository extends R2dbcRepository<EmployeEntity, UUID> {

    Flux<EmployeEntity> findByAgenceVoyageId(UUID agenceId);
}