package cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.r2dbc;

import cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.entity.OrganizationEntity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.data.r2dbc.repository.R2dbcRepository;

import java.util.UUID;

public interface OrganizationR2dbcRepository extends R2dbcRepository<OrganizationEntity, UUID> {
  Mono<OrganizationEntity> findByOrganizationId(UUID organizationId);
  Flux<OrganizationEntity> findByCreatedBy(UUID createdBy);
}
