package cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.adapter;

import cm.yowyob.bus_station_backend.application.port.out.CoordonneePersistencePort;
import cm.yowyob.bus_station_backend.domain.model.Coordonnee;
import cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.entity.CoordonneeEntity;
import cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.mapper.CoordonneePersistenceMapper;
import cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.r2dbc.CoordonneeR2dbcRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CoordonneePersistenceAdapter implements CoordonneePersistencePort {

    private final CoordonneeR2dbcRepository repository;
    private final CoordonneePersistenceMapper mapper;

    @Override
    public Mono<Coordonnee> save(Coordonnee coordonnee) {
        CoordonneeEntity entity = mapper.toEntity(coordonnee);
        if (coordonnee.getIdCoordonnee() == null) {
            entity.setIdCoordonnee(UUID.randomUUID());
            entity.setAsNew();
        }
        return repository.save(entity)
                .map(mapper::toDomain);
    }

    @Override
    public Mono<Coordonnee> findById(UUID id) {
        return repository.findById(id)
                .map(mapper::toDomain);
    }
}
