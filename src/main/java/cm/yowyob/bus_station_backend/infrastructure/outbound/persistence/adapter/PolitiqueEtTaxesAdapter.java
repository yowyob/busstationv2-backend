package cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.adapter;

import java.util.UUID;

import org.springframework.stereotype.Component;

import cm.yowyob.bus_station_backend.application.port.out.PolitiqueEtTaxesPort;
import cm.yowyob.bus_station_backend.domain.enums.PolitiqueOuTaxe;
import cm.yowyob.bus_station_backend.domain.model.PolitiqueEtTaxes;
import cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.entity.PolitiqueEtTaxesEntity;
import cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.mapper.PolitiqueEtTaxesPersistenceMapper;
import cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.r2dbc.PolitiqueEtTaxesR2bcRepository;
import lombok.AllArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@AllArgsConstructor
public class PolitiqueEtTaxesAdapter implements PolitiqueEtTaxesPort {

    private final PolitiqueEtTaxesR2bcRepository politiqueEtTaxesRepository;
    private final PolitiqueEtTaxesPersistenceMapper politiqueEtTaxesPersistenceMapper;

    @Override
    public Mono<PolitiqueEtTaxes> save(PolitiqueEtTaxes politiqueEtTaxes) {
        PolitiqueEtTaxesEntity entity = politiqueEtTaxesPersistenceMapper.toEntity(politiqueEtTaxes);

        if (entity.getIdPolitique() == null) {
            entity.setIdPolitique(UUID.randomUUID());
            entity.setAsNew();
            return politiqueEtTaxesRepository.save(entity)
                    .map(politiqueEtTaxesPersistenceMapper::toDomain);
        }

        // ID fourni : déterminer INSERT vs UPDATE
        return politiqueEtTaxesRepository.existsById(entity.getIdPolitique())
                .flatMap(exists -> {
                    if (Boolean.TRUE.equals(exists)) {
                        // UPDATE : isNew=false par défaut
                        return politiqueEtTaxesRepository.save(entity);
                    } else {
                        entity.setAsNew();
                        return politiqueEtTaxesRepository.save(entity);
                    }
                })
                .map(politiqueEtTaxesPersistenceMapper::toDomain);
    }

    @Override
    public Mono<PolitiqueEtTaxes> findById(UUID politiqueId) {
        return politiqueEtTaxesRepository.findById(politiqueId)
                .map(politiqueEtTaxesPersistenceMapper::toDomain);
    }

    @Override
    public Flux<PolitiqueEtTaxes> findByGareRoutiereId(UUID gareRoutiereId) {
        return politiqueEtTaxesRepository.findByGareRoutiereId(gareRoutiereId)
                .map(politiqueEtTaxesPersistenceMapper::toDomain);
    }

    @Override
    public Mono<Void> deleteById(UUID politiqueId) {
        return politiqueEtTaxesRepository.deleteById(politiqueId);
    }

    @Override
    public Flux<PolitiqueEtTaxes> findByGareRoutiereIdAndType(UUID gareRoutiereId, PolitiqueOuTaxe type) {
        return politiqueEtTaxesRepository
                .findByGareRoutiereIdAndType(gareRoutiereId, type.name())
                .map(politiqueEtTaxesPersistenceMapper::toDomain);
    }
}
