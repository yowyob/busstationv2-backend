package cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.adapter;

import cm.yowyob.bus_station_backend.application.port.out.PlanningPersistencePort;
import cm.yowyob.bus_station_backend.domain.model.CreneauPlanning;
import cm.yowyob.bus_station_backend.domain.model.PlanningVoyage;
import cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.mapper.CreneauPlanningPersistenceMapper;
import cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.mapper.PlanningVoyagePersistenceMapper;
import cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.r2dbc.CreneauPlanningR2dbcRepository;
import cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.r2dbc.PlanningVoyageR2dbcRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PlanningPersistenceAdapter implements PlanningPersistencePort {

    private final PlanningVoyageR2dbcRepository planningRepository;
    private final CreneauPlanningR2dbcRepository creneauRepository;
    private final PlanningVoyagePersistenceMapper planningMapper;
    private final CreneauPlanningPersistenceMapper creneauMapper;

    // --- PlanningVoyage ---

    @Override
    public Mono<PlanningVoyage> save(PlanningVoyage planning) {
        return planningRepository.findById(planning.getIdPlanning())
                .flatMap(existing -> {
                    // Update
                    var entity = planningMapper.toEntity(planning);
                    return planningRepository.save(entity);
                })
                .switchIfEmpty(Mono.defer(() -> {
                    // Insert
                    var entity = planningMapper.toNewEntity(planning);
                    return planningRepository.save(entity);
                }))
                .map(planningMapper::toDomain);
    }

    @Override
    public Mono<PlanningVoyage> findById(UUID planningId) {
        return planningRepository.findById(planningId)
                .map(planningMapper::toDomain);
    }

    @Override
    public Mono<Void> deleteById(UUID planningId) {
        return planningRepository.deleteById(planningId);
    }

    @Override
    public Flux<PlanningVoyage> findByAgenceId(UUID agenceId) {
        return planningRepository.findByIdAgenceVoyage(agenceId)
                .map(planningMapper::toDomain);
    }

    @Override
    public Flux<PlanningVoyage> findActifsByAgenceId(UUID agenceId) {
        return planningRepository.findActifsByIdAgenceVoyage(agenceId)
                .map(planningMapper::toDomain);
    }

    // --- CreneauPlanning ---

    @Override
    public Mono<CreneauPlanning> saveCreneau(CreneauPlanning creneau) {
        return creneauRepository.findById(creneau.getIdCreneau())
                .flatMap(existing -> {
                    var entity = creneauMapper.toEntity(creneau);
                    return creneauRepository.save(entity);
                })
                .switchIfEmpty(Mono.defer(() -> {
                    var entity = creneauMapper.toNewEntity(creneau);
                    return creneauRepository.save(entity);
                }))
                .map(creneauMapper::toDomain);
    }

    @Override
    public Mono<CreneauPlanning> findCreneauById(UUID creneauId) {
        return creneauRepository.findById(creneauId)
                .map(creneauMapper::toDomain);
    }

    @Override
    public Mono<Void> deleteCreneauById(UUID creneauId) {
        return creneauRepository.deleteById(creneauId);
    }

    @Override
    public Flux<CreneauPlanning> findCreneauxByPlanningId(UUID planningId) {
        return creneauRepository.findByIdPlanning(planningId)
                .map(creneauMapper::toDomain);
    }

    @Override
    public Flux<CreneauPlanning> findCreneauxActifsByPlanningId(UUID planningId) {
        return creneauRepository.findActifsByIdPlanning(planningId)
                .map(creneauMapper::toDomain);
    }

    @Override
    public Mono<Void> deleteCreneauxByPlanningId(UUID planningId) {
        return creneauRepository.deleteByIdPlanning(planningId);
    }
}
