package cm.yowyob.bus_station_backend.application.port.in;

import cm.yowyob.bus_station_backend.application.dto.alerte.AlerteCreateDTO;
import cm.yowyob.bus_station_backend.domain.model.AlerteAgence;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface AlerteUseCase {

    /**
     * Envoyer une alerte depuis un BSM vers une agence.
     */
    Mono<AlerteAgence> createAlerte(AlerteCreateDTO dto, UUID gareId, UUID bsmId);

    /**
     * Historique des alertes envoyées par une gare.
     */
    Flux<AlerteAgence> getAlertesByGare(UUID gareId);

    /**
     * Alertes reçues par une agence.
     */
    Flux<AlerteAgence> getAlertesByAgence(UUID agenceId);

    /**
     * Marquer une alerte comme lue.
     */
    Mono<AlerteAgence> marquerLu(UUID alerteId);
}