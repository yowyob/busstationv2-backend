package cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.adapter;

import cm.yowyob.bus_station_backend.application.port.out.VoyageBrouillonPersistencePort;
import cm.yowyob.bus_station_backend.domain.model.VoyageBrouillon;
import cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.entity.VoyageBrouillonEntity;
import cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.mapper.VoyageBrouillonPersistenceMapper;
import cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.r2dbc.VoyageBrouillonR2dbcRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class VoyageBrouillonPersistenceAdapter implements VoyageBrouillonPersistencePort {

    private final VoyageBrouillonR2dbcRepository repository;
    private final VoyageBrouillonPersistenceMapper mapper;

    @Override
    public Mono<VoyageBrouillon> save(VoyageBrouillon brouillon) {
        VoyageBrouillonEntity entity = mapper.toEntity(brouillon);
        LocalDateTime now = LocalDateTime.now();

        if (brouillon.getId() == null) {
            entity.setId(UUID.randomUUID());
            entity.setCreatedAt(now);
            entity.setUpdatedAt(now);
            entity.setAsNew();
            return repository.save(entity).map(mapper::toDomain);
        }
        // Pattern LOT 3 : existsById pour distinguer INSERT/UPDATE
        return repository.existsById(brouillon.getId())
                .flatMap(exists -> {
                    if (!exists) {
                        if (entity.getCreatedAt() == null) entity.setCreatedAt(now);
                        entity.setAsNew();
                    }
                    entity.setUpdatedAt(now);
                    return repository.save(entity);
                })
                .map(mapper::toDomain);
    }

    @Override
    public Mono<VoyageBrouillon> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Flux<VoyageBrouillon> findByAgence(UUID agenceId, String statut) {
        return repository.findByAgence(agenceId, statut).map(mapper::toDomain);
    }

    @Override
    public Mono<Void> deleteById(UUID id) {
        return repository.deleteById(id);
    }
}