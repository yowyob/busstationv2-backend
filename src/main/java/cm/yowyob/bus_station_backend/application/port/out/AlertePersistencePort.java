package cm.yowyob.bus_station_backend.application.port.out;

import cm.yowyob.bus_station_backend.domain.model.AlerteAgence;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface AlertePersistencePort {

    Mono<AlerteAgence> save(AlerteAgence alerte);

    Mono<AlerteAgence> findById(UUID alerteId);

    Flux<AlerteAgence> findByGareId(UUID gareId);

    Flux<AlerteAgence> findByAgenceId(UUID agenceId);

    Mono<AlerteAgence> update(AlerteAgence alerte);
}