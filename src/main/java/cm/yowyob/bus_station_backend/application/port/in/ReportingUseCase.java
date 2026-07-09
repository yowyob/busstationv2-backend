package cm.yowyob.bus_station_backend.application.port.in;

import cm.yowyob.bus_station_backend.application.dto.statistic.AgenceEvolutionDTO;
import cm.yowyob.bus_station_backend.application.dto.statistic.AgenceStatisticsDTO;
import cm.yowyob.bus_station_backend.domain.model.Historique;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface ReportingUseCase {
    // Statistiques Agence
    Mono<AgenceStatisticsDTO> getAgenceStatistics(UUID agenceId);
    Mono<AgenceEvolutionDTO> getAgenceEvolution(UUID agenceId);

    // Historique
    Flux<Historique> getUserHistory(UUID userId);
    Mono<Historique> getHistoryDetails(UUID id);

    // Historique complet pour une agence (Dashboard Agence)
    Flux<Historique> getHistoryByAgence(UUID agenceId);
}
