package cm.yowyob.bus_station_backend.application.port.out;

import cm.yowyob.bus_station_backend.domain.model.CreneauPlanning;
import cm.yowyob.bus_station_backend.domain.model.PlanningVoyage;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface PlanningPersistencePort {

    // --- PlanningVoyage ---
    Mono<PlanningVoyage> save(PlanningVoyage planning);

    Mono<PlanningVoyage> findById(UUID planningId);

    Mono<Void> deleteById(UUID planningId);

    Flux<PlanningVoyage> findByAgenceId(UUID agenceId);

    Flux<PlanningVoyage> findActifsByAgenceId(UUID agenceId);

    // --- CreneauPlanning ---
    Mono<CreneauPlanning> saveCreneau(CreneauPlanning creneau);

    Mono<CreneauPlanning> findCreneauById(UUID creneauId);

    Mono<Void> deleteCreneauById(UUID creneauId);

    Flux<CreneauPlanning> findCreneauxByPlanningId(UUID planningId);

    Flux<CreneauPlanning> findCreneauxActifsByPlanningId(UUID planningId);

    Mono<Void> deleteCreneauxByPlanningId(UUID planningId);
}