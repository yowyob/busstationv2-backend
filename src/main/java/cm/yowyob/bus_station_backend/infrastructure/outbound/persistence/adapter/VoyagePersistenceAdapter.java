package cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.adapter;

import cm.yowyob.bus_station_backend.application.port.out.VoyagePersistencePort;
import cm.yowyob.bus_station_backend.domain.model.ClassVoyage;
import cm.yowyob.bus_station_backend.domain.model.LigneVoyage;
import cm.yowyob.bus_station_backend.domain.model.Voyage;
import cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.entity.ClassVoyageEntity;
import cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.entity.LigneVoyageEntity;
import cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.entity.VoyageEntity;
import cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.mapper.ClassVoyagePersistenceMapper;
import cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.mapper.LigneVoyagePersistenceMapper;
import cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.mapper.VoyagePersistenceMapper;
import cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.r2dbc.ClassVoyageR2dbcRepository;
import cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.r2dbc.LigneVoyageR2dbcRepository;
import cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.r2dbc.VoyageR2dbcRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class VoyagePersistenceAdapter implements VoyagePersistencePort {

    private final VoyageR2dbcRepository voyageRepository;
    private final LigneVoyageR2dbcRepository ligneRepository;
    private final ClassVoyageR2dbcRepository classRepository;

    private final VoyagePersistenceMapper voyageMapper;
    private final LigneVoyagePersistenceMapper ligneMapper;
    private final ClassVoyagePersistenceMapper classMapper;

    // ------------------ VOYAGE ------------------

    @Override
    public Mono<Voyage> save(Voyage voyage) {
        VoyageEntity entity = voyageMapper.toEntity(voyage);
        if (voyage.getIdVoyage() == null) {
            entity.setIdVoyage(UUID.randomUUID());
            entity.setAsNew();
            return voyageRepository.save(entity).map(voyageMapper::toDomain);
        }
        // Vérifie si l'entité existe déjà → UPDATE; sinon → INSERT
        return voyageRepository.existsById(voyage.getIdVoyage())
                .flatMap(exists -> {
                    if (!exists) {
                        entity.setAsNew();
                    }
                    return voyageRepository.save(entity);
                })
                .map(voyageMapper::toDomain);
    }

    @Override
    public Mono<Voyage> findById(UUID id) {
        return voyageRepository.findById(id)
                .map(voyageMapper::toDomain);
    }

    @Override
    public Flux<Voyage> findAll(Pageable pageable) {
        return voyageRepository.findAllPaged(pageable)
                .map(voyageMapper::toDomain);
    }

    @Override
    public Flux<Voyage> findByAgenceId(UUID agenceId, Pageable pageable) {
        return voyageRepository.findByAgenceIdPaged(agenceId, pageable)
                .map(voyageMapper::toDomain);
    }

    @Override
    public Flux<Voyage> findByPointName(String pointName, Pageable pageable) {
        return voyageRepository.findByPointNamePaged(pointName, pageable)
                .map(voyageMapper::toDomain);
    }

    @Override
    public Mono<Void> deleteById(UUID id) {
        return voyageRepository.deleteById(id);
    }

    @Override
    public Mono<Long> countVoyagesByAgenceId(UUID agenceId) {
        return agenceId == null
                ? voyageRepository.count()
                : voyageRepository.countByAgenceId(agenceId);
    }

    @Override
    public Mono<Long> countVoyagesByPointName(String pointName) {
        return voyageRepository.countByPointName(pointName);
    }

    // ------------------ LIGNE VOYAGE ------------------

    @Override
    public Mono<LigneVoyage> saveLigneVoyage(LigneVoyage ligneVoyage) {
        LigneVoyageEntity entity = ligneMapper.toEntity(ligneVoyage);
        if (ligneVoyage.getIdLigneVoyage() == null) {
            entity.setIdLigneVoyage(UUID.randomUUID());
            entity.setAsNew();
            return ligneRepository.save(entity).map(ligneMapper::toDomain);
        }
        return ligneRepository.existsById(ligneVoyage.getIdLigneVoyage())
                .flatMap(exists -> {
                    if (!exists) {
                        entity.setAsNew();
                    }
                    return ligneRepository.save(entity);
                })
                .map(ligneMapper::toDomain);
    }

    @Override
    public Mono<LigneVoyage> findLigneVoyageByVoyageId(UUID voyageId) {
        return ligneRepository.findByIdVoyage(voyageId)
                .map(ligneMapper::toDomain);
    }

    @Override
    public Flux<LigneVoyage> findLignesVoyageByAgenceId(UUID agenceId) {
        return ligneRepository.findByIdAgenceVoyage(agenceId)
                .map(ligneMapper::toDomain);
    }

    // ------------------ CLASS VOYAGE ------------------

    @Override
    public Mono<ClassVoyage> saveClassVoyage(ClassVoyage classVoyage) {

        ClassVoyageEntity entity = classMapper
                .toEntity(classVoyage);
        if (entity.getId() == null) {
            entity.setId(UUID.randomUUID());
        }
        entity.setAsNew(); // toujours INSERT
        return classRepository.save(entity)
                .map(classMapper::toDomain);
    }

    @Override
    public Mono<ClassVoyage> findClassVoyageById(UUID id) {
        return classRepository.findById(id)
                .map(classMapper::toDomain);
    }

    @Override
    public Flux<ClassVoyage> findAllClassVoyages(Pageable pageable) {
        return classRepository.findAllBy(pageable)
                .map(classMapper::toDomain);
    }

    @Override
    public Flux<ClassVoyage> findClassVoyagesByAgence(UUID agenceId) {
        return classRepository.findByIdAgenceVoyage(agenceId)
                .map(classMapper::toDomain);
    }

    @Override
    public Mono<Void> deleteClassVoyageById(UUID id) {
        return classRepository.deleteById(id);
    }

    // ------------------ OCCUPIED PLACES ------------------

    @Override
    public Flux<Integer> findOccupiedPlacesByVoyageId(UUID voyageId) {
        return voyageRepository.findOccupiedPlacesByVoyageId(voyageId);
    }

    // ------------------ LOT 3 ------------------

    @Override
    public Flux<Voyage> findPublicByAgenceId(UUID agenceId, java.time.LocalDateTime now, Pageable pageable) {
        return voyageRepository.findPublicByAgenceId(agenceId, now, pageable)
                .map(voyageMapper::toDomain);
    }

    @Override
    public Mono<Long> countPublicByAgenceId(UUID agenceId, java.time.LocalDateTime now) {
        return voyageRepository.countPublicByAgenceId(agenceId, now);
    }

    @Override
    public Flux<Voyage> findSimilaires(UUID excludeId, String lieuDepart, String lieuArrive, UUID agenceId, java.time.LocalDateTime now, int limit) {
        return voyageRepository.findSimilairesByTrajet(excludeId, lieuDepart, lieuArrive, now, limit)
                .switchIfEmpty(voyageRepository.findSimilairesByAgence(excludeId, agenceId, now, limit))
                .take(limit)
                .map(voyageMapper::toDomain);
    }

    @Override
    public Flux<Voyage> searchVoyages(String lieuDepart, String lieuArrive, String date, UUID classId, UUID agenceId, Pageable pageable) {
        return voyageRepository.searchVoyages(lieuDepart, lieuArrive, date, classId, agenceId, pageable)
                .map(voyageMapper::toDomain);
    }

    @Override
    public Mono<Long> countSearchVoyages(String lieuDepart, String lieuArrive, String date, UUID classId,
            UUID agenceId) {
        return voyageRepository.countSearchVoyages(lieuDepart, lieuArrive, date, classId, agenceId);
    }
    
    // ------------------ LOT 5 : Disponibilité ------------------

    @Override
    public Flux<UUID> findVoyagesUsingVehiculeBetween(UUID vehiculeId, java.time.LocalDateTime start, java.time.LocalDateTime end) {
        return voyageRepository.findVoyagesUsingVehiculeBetween(vehiculeId, start, end);
    }

    @Override
    public Flux<UUID> findVoyagesUsingChauffeurBetween(UUID chauffeurId, java.time.LocalDateTime start,
            java.time.LocalDateTime end) {
        return voyageRepository.findVoyagesUsingChauffeurBetween(chauffeurId, start, end);
    }
    
    // ------------------ LOT 8 : Voyages par gare ------------------

    @Override
    public Flux<Voyage> findByGareRoutiereIdAndDate(UUID gareId, java.time.LocalDate date, Pageable pageable) {
        String dateStr = (date == null) ? null : date.toString();
        return voyageRepository.findByGareIdAndDateOptional(gareId, dateStr, pageable)
                .map(voyageMapper::toDomain);
    }

    @Override
    public Mono<Long> countByGareRoutiereIdAndDate(UUID gareId, java.time.LocalDate date) {
        String dateStr = (date == null) ? null : date.toString();
        return voyageRepository.countByGareIdAndDateOptional(gareId, dateStr);
    }

    // ------------------ LOT 8 : Statistiques BSM ------------------

    @Override
    public Mono<Long> countVoyagesByGareIdAndDate(UUID gareId, java.time.LocalDate date) {
        String dateStr = (date == null) ? null : date.toString();
        return voyageRepository.countByGareIdAndDateOptional(gareId, dateStr);
    }

    @Override
    public Mono<Long> countPublicVoyagesByGareIdAfter(UUID gareId, java.time.LocalDateTime now) {
        return voyageRepository.countPublicByGareIdAfter(gareId, now);
    }

    @Override
    public Mono<Double> avgTauxRemplissageByGareId(UUID gareId) {
        return voyageRepository.avgTauxRemplissageByGareId(gareId)
                .defaultIfEmpty(0.0);
    }
}
