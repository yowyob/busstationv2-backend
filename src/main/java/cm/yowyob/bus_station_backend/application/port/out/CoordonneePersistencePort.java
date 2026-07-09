package cm.yowyob.bus_station_backend.application.port.out;

import java.util.UUID;

import cm.yowyob.bus_station_backend.domain.model.Coordonnee;
import reactor.core.publisher.Mono;

public interface CoordonneePersistencePort {
    Mono<Coordonnee> save(Coordonnee coordonnee);

    Mono<Coordonnee> findById(UUID id);
}
