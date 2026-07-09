package cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.adapter;

import cm.yowyob.bus_station_backend.application.port.out.AgencePersistencePort;
import cm.yowyob.bus_station_backend.domain.model.AgenceVoyage;
import cm.yowyob.bus_station_backend.domain.model.PolitiqueAnnulation;
import cm.yowyob.bus_station_backend.domain.model.Vehicule;
import cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.entity.AgenceVoyageEntity;
import cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.entity.PolitiqueAnnulationEntity;
import cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.entity.VehiculeEntity;
import cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.mapper.AgenceVoyagePersistenceMapper;
import cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.mapper.PolitiqueAnnulationPersistenceMapper;
import cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.mapper.TauxPeriodePersistenceMapper;
import cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.mapper.VehiculePersistenceMapper;
import cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.r2dbc.AgenceVoyageR2dbcRepository;
import cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.r2dbc.PolitiqueAnnulationR2dbcRepository;
import cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.r2dbc.TauxPeriodeR2dbcRepository;
import cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.r2dbc.VehiculeR2dbcRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Query;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AgencePersistenceAdapter implements AgencePersistencePort {

    private final AgenceVoyageR2dbcRepository agenceRepository;
    private final VehiculeR2dbcRepository vehiculeRepository;
    private final PolitiqueAnnulationR2dbcRepository politiqueRepository;
    private final TauxPeriodeR2dbcRepository tauxPeriodeRepository;
    private final R2dbcEntityTemplate r2dbcEntityTemplate;

    private final AgenceVoyagePersistenceMapper agenceMapper;
    private final VehiculePersistenceMapper vehiculeMapper;
    private final PolitiqueAnnulationPersistenceMapper politiqueMapper;
    private final TauxPeriodePersistenceMapper tauxPeriodeMapper;

    @Override
    public Mono<AgenceVoyage> save(AgenceVoyage agence) {
        AgenceVoyageEntity entity = agenceMapper.toEntity(agence);
        if (agence.getAgencyId() == null) {
            entity.setAgencyId(UUID.randomUUID());
            entity.setAsNew();
            return agenceRepository.save(entity).map(agenceMapper::toDomain);
        }
        
        return agenceRepository.existsById(agence.getAgencyId())
                .flatMap(exists -> {
                    if (!exists) {
                        entity.setAsNew();
                    }
                    return agenceRepository.save(entity);
                })
                .map(agenceMapper::toDomain);
    }

    @Override
    public Mono<AgenceVoyage> findById(UUID id) {
        return agenceRepository.findById(id)
                .map(agenceMapper::toDomain);
    }

    @Override
    public Mono<AgenceVoyage> findByChefAgenceId(UUID userId) {
        return agenceRepository.findByUserId(userId)
                .map(agenceMapper::toDomain);
    }

    @Override
    public Flux<AgenceVoyage> findByOrganisationId(UUID organizationId) {
        return agenceRepository.findByOrganisationId(organizationId)
                .map(agenceMapper::toDomain);
    }

    @Override
    public Mono<Page<AgenceVoyage>> findAll(Pageable pageable) {
        Query query = Query.empty().with(pageable);

        Mono<List<AgenceVoyage>> content = r2dbcEntityTemplate.select(query, AgenceVoyageEntity.class)
                .map(agenceMapper::toDomain)
                .collectList();

        Mono<Long> count = agenceRepository.count();

        return Mono.zip(content, count)
                .map(tuple -> new PageImpl<>(tuple.getT1(), pageable, tuple.getT2()));
    }

    @Override
    public Mono<Boolean> existsByLongName(String longName) {
        return agenceRepository.existsByLongName(longName);
    }

    @Override
    public Mono<Boolean> existsByShortName(String shortName) {
        return agenceRepository.existsByShortName(shortName);
    }

    @Override
    public Mono<Void> deleteById(UUID id) {
        return agenceRepository.deleteById(id);
    }

    @Override
    public Flux<AgenceVoyage> findByGareRoutiereId(UUID gareRoutiereId) {
        return agenceRepository.findByGareRoutiereId(gareRoutiereId)
                .map(agenceMapper::toDomain);
    }

    // ---------- VEHICULE ----------

    @Override
    public Mono<Vehicule> saveVehicule(Vehicule vehicule) {
        VehiculeEntity entity = vehiculeMapper.toEntity(vehicule);
        if (vehicule.getIdVehicule() == null) {
            entity.setIdVehicule(UUID.randomUUID());
            entity.setAsNew();
        }
        return vehiculeRepository.save(entity)
                .map(vehiculeMapper::toDomain);
    }

    @Override
    public Mono<Vehicule> findVehiculeById(UUID id) {
        return vehiculeRepository.findById(id)
                .map(vehiculeMapper::toDomain);
    }

    @Override
    public Flux<Vehicule> findVehiculesByAgenceId(UUID agenceId) {
        return vehiculeRepository.findByIdAgenceVoyage(agenceId)
                .map(vehiculeMapper::toDomain);
    }

    @Override
    public Mono<Void> deleteVehiculeById(UUID id) {
        return vehiculeRepository.deleteById(id);
    }

    // ---------- POLITIQUE ANNULATION ----------

    @Override
    public Mono<PolitiqueAnnulation> savePolitique(PolitiqueAnnulation politique) {
        PolitiqueAnnulationEntity entity = politiqueMapper.toEntity(politique);
        if (politique.getIdPolitique() == null){
            entity.setIdPolitique(UUID.randomUUID());
            entity.setAsNew();
        }

        return politiqueRepository.save(entity)
                .flatMap(saved -> tauxPeriodeRepository.deleteByIdPolitiqueAnnulation(saved.getIdPolitique())
                        .thenMany(
                                Flux.fromIterable(politique.getListeTauxPeriode())
                                        .map(taux -> tauxPeriodeRepository.save(
                                                tauxPeriodeMapper.toEntity(taux))))
                        .then(
                                findPolitiqueById(saved.getIdPolitique())));
    }

    @Override
    public Mono<PolitiqueAnnulation> findPolitiqueByAgenceId(UUID agenceId) {
        return politiqueRepository.findByIdAgenceVoyage(agenceId)
                .map(politiqueMapper::toDomain);
    }

    @Override
    public Mono<PolitiqueAnnulation> findPolitiqueById(UUID id) {
        return politiqueRepository.findById(id)
                .map(politiqueMapper::toDomain);
    }

    private Mono<PolitiqueAnnulation> buildAggregate(PolitiqueAnnulationEntity entity) {
        PolitiqueAnnulation politique = politiqueMapper.toDomain(entity);

        return tauxPeriodeRepository.findByIdPolitiqueAnnulation(entity.getIdPolitique())
                .collectList()
                .map(taux -> politiqueMapper.enrichWithTaux(politique, taux));
    }
}
