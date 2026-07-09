package cm.yowyob.bus_station_backend.application.port.out;

import cm.yowyob.bus_station_backend.domain.model.Organization;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface OrganizationPersistencePort {
    Mono<Organization> save(Organization organization);

    Mono<Organization> findById(UUID id);

    Mono<Organization> findByOrganisationId(UUID organizationId);

    Flux<Organization> findByCreatedBy(UUID createdBy);
}
