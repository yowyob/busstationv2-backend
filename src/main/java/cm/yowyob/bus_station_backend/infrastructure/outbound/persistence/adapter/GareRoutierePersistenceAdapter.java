package cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.adapter;

import cm.yowyob.bus_station_backend.application.port.out.GareRoutierePersistencePort;
import cm.yowyob.bus_station_backend.domain.enums.ServicesGareRoutiere;
import cm.yowyob.bus_station_backend.domain.model.GareRoutiere;
import cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.entity.GareRoutiereEntity;
import cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.mapper.GareRoutierePersistenceMapper;
import cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.r2dbc.GareRoutiereR2dbcRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class GareRoutierePersistenceAdapter implements GareRoutierePersistencePort {

    private final GareRoutiereR2dbcRepository gareRoutiereR2dbcRepository;
    private final GareRoutierePersistenceMapper gareRoutierePersistenceMapper;
    private final R2dbcEntityTemplate r2dbcEntityTemplate;

    @Override
    public Mono<GareRoutiere> saveGareRoutiere(GareRoutiere gareRoutiere) {
        GareRoutiereEntity entity = gareRoutierePersistenceMapper.toEntity(gareRoutiere);
        if (entity.getIdGareRoutiere() == null) {
            entity.setIdGareRoutiere(UUID.randomUUID());
            entity.setAsNew();
            return gareRoutiereR2dbcRepository.save(entity).map(gareRoutierePersistenceMapper::toDomain);
        }
        
        return gareRoutiereR2dbcRepository.existsById(entity.getIdGareRoutiere())
                .flatMap(exists -> {
                    if (!exists) {
                        entity.setAsNew();
                    }
                    return gareRoutiereR2dbcRepository.save(entity);
                })
                .map(gareRoutierePersistenceMapper::toDomain);
    }

    @Override
    public Mono<GareRoutiere> getGareRoutiereByManagerId(UUID managerId) {
        return gareRoutiereR2dbcRepository.findByManagerId(managerId)
                .next()
                .map(gareRoutierePersistenceMapper::toDomain);
    }

    @Override
    public Mono<Page<GareRoutiere>> findAll(String searchTerm, List<ServicesGareRoutiere> services, Pageable pageable) {

        Criteria criteria = Criteria.empty();

        if (searchTerm != null && !searchTerm.isBlank()) {
            Criteria searchCriteria = Criteria.where("nom_gare_routiere").like("%" + searchTerm + "%").ignoreCase(true)
                    .or(Criteria.where("ville").like("%" + searchTerm + "%").ignoreCase(true));
            criteria = criteria.and(searchCriteria);
        }

        if (services != null && !services.isEmpty()) {

            Criteria servicesCriteria = Criteria.empty();
            for (ServicesGareRoutiere service : services) {
                servicesCriteria = servicesCriteria.or(Criteria.where("services").like("%" + service.name() + "%").ignoreCase(true));
            }
            criteria = criteria.and(servicesCriteria);
        }

        Query query = Query.query(criteria).with(pageable);

        Mono<List<GareRoutiere>> content = r2dbcEntityTemplate.select(query, GareRoutiereEntity.class)
                .map(gareRoutierePersistenceMapper::toDomain)
                .collectList();

        Mono<Long> count = r2dbcEntityTemplate.count(Query.query(criteria), GareRoutiereEntity.class);

        return Mono.zip(content, count)
                .map(tuple -> new PageImpl<>(tuple.getT1(), pageable, tuple.getT2()));

    }

    @Override
    public Mono<GareRoutiere> getGareRoutiereById(UUID gareRoutiereId) {
        if (gareRoutiereId == null)
            return Mono.empty();
        return gareRoutiereR2dbcRepository.findById(gareRoutiereId)
                .map(gareRoutierePersistenceMapper::toDomain);
    }

    @Override
    public Mono<Long> count() {
        return gareRoutiereR2dbcRepository.count();
    }

    @Override
    public Mono<GareRoutiere> updateGareRoutiere(GareRoutiere gareRoutiere) {
        if (gareRoutiere.getIdGareRoutiere() == null) {
            return Mono.empty();
        }
        return gareRoutiereR2dbcRepository.findById(gareRoutiere.getIdGareRoutiere())
                .flatMap(existing -> {
                    GareRoutiereEntity updated = gareRoutierePersistenceMapper.toEntity(gareRoutiere);
                    updated.setVersion(existing.getVersion());
                    return gareRoutiereR2dbcRepository.save(updated);
                })
                .map(gareRoutierePersistenceMapper::toDomain);
    }
}
