package cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.adapter;

import cm.yowyob.bus_station_backend.application.port.out.OrganizationPersistencePort;
import cm.yowyob.bus_station_backend.domain.model.Organization;
import cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.entity.OrganizationEntity;
import cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.mapper.OrganizationPersistenceMapper;
import cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.r2dbc.OrganizationR2dbcRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OrganizationPersistenceAdapter implements OrganizationPersistencePort {
    private final OrganizationR2dbcRepository repository;
    private final OrganizationPersistenceMapper mapper;

    @Override
    public Mono<Organization> save(Organization organization) {
        OrganizationEntity entity = mapper.toEntity(organization);
        if (organization.getId() == null) {
            entity.setId(UUID.randomUUID());
            entity.setAsNew();
        }
        return repository.save(entity).map(mapper::toDomain);
    }

    @Override
    public Mono<Organization> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Mono<Organization> findByOrganisationId(UUID organizationId) {
        return repository.findByOrganizationId(organizationId).map(mapper::toDomain);
    }

    @Override
    public Flux<Organization> findByCreatedBy(UUID createdBy) {
        return repository.findByCreatedBy(createdBy).map(mapper::toDomain);
    }
}
