package cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.mapper;

import cm.yowyob.bus_station_backend.domain.model.SoldeIndemnisation;
import cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.entity.SoldeIndemnisationEntity;
import org.springframework.stereotype.Component;

@Component
public class SoldeIndemnisationPersistenceMapper {

    public SoldeIndemnisation toDomain(SoldeIndemnisationEntity entity) {
        if (entity == null) return null;

        return SoldeIndemnisation.builder()
                .idSolde(entity.getIdSolde())
                .solde(entity.getSolde())
                .type(entity.getType())
                .idUser(entity.getIdUser())
                .idAgenceVoyage(entity.getIdAgenceVoyage())
                .build();
    }

    public SoldeIndemnisationEntity toEntity(SoldeIndemnisation domain) {
        if (domain == null) return null;

        return SoldeIndemnisationEntity.builder()
                .idSolde(domain.getIdSolde())
                .solde(domain.getSolde())
                .type(domain.getType())
                .idUser(domain.getIdUser())
                .idAgenceVoyage(domain.getIdAgenceVoyage())
                .build();
    }
}

