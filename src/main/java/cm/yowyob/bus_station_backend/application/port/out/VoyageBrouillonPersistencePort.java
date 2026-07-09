package cm.yowyob.bus_station_backend.application.port.out;

import cm.yowyob.bus_station_backend.domain.model.VoyageBrouillon;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface VoyageBrouillonPersistencePort {
    Mono<VoyageBrouillon> save(VoyageBrouillon brouillon);

    Mono<VoyageBrouillon> findById(UUID id);

    Flux<VoyageBrouillon> findByAgence(UUID agenceId, String statut);

    Mono<Void> deleteById(UUID id);
}