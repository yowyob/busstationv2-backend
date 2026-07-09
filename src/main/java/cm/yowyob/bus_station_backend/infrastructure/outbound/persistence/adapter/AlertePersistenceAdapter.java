package cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.adapter;

import cm.yowyob.bus_station_backend.application.port.out.AlertePersistencePort;
import cm.yowyob.bus_station_backend.domain.model.AlerteAgence;
import cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.entity.AlerteAgenceEntity;
import cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.mapper.AlertePersistenceMapper;
import cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.r2dbc.AlerteR2dbcRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AlertePersistenceAdapter implements AlertePersistencePort {

    private final AlerteR2dbcRepository alerteRepository;
    private final AlertePersistenceMapper alerteMapper;

    @Override
    public Mono<AlerteAgence> save(AlerteAgence alerte) {
        AlerteAgenceEntity entity = alerteMapper.toEntity(alerte);
        // INSERT : toujours une nouvelle entité
        entity.setAsNew();
        return alerteRepository.save(entity)
                .map(alerteMapper::toDomain);
    }

    @Override
    public Mono<AlerteAgence> findById(UUID alerteId) {
        return alerteRepository.findById(alerteId)
                .map(alerteMapper::toDomain);
    }

    @Override
    public Flux<AlerteAgence> findByGareId(UUID gareId) {
        return alerteRepository.findByGareId(gareId)
                .map(alerteMapper::toDomain);
    }

    @Override
    public Flux<AlerteAgence> findByAgenceId(UUID agenceId) {
        return alerteRepository.findByAgenceId(agenceId)
                .map(alerteMapper::toDomain);
    }

    @Override
    public Mono<AlerteAgence> update(AlerteAgence alerte) {
        // UPDATE : l'entité existe déjà — on lit d'abord pour préserver les champs internes
        return alerteRepository.findById(alerte.getIdAlerte())
                .flatMap(existing -> {
                    AlerteAgenceEntity updated = alerteMapper.toEntity(alerte);
                    // isNew = false par défaut dans le builder → Spring fait un UPDATE
                    return alerteRepository.save(updated);
                })
                .map(alerteMapper::toDomain);
    }
}