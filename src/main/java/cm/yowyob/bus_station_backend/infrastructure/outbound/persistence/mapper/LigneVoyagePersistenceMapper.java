package cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.mapper;

import cm.yowyob.bus_station_backend.domain.model.LigneVoyage;
import cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.entity.LigneVoyageEntity;
import org.springframework.stereotype.Component;

@Component
public class LigneVoyagePersistenceMapper {

    public LigneVoyage toDomain(LigneVoyageEntity entity) {
        if (entity == null) return null;

        return LigneVoyage.builder()
                .idLigneVoyage(entity.getIdLigneVoyage())
                .idClassVoyage(entity.getIdClassVoyage())
                .idVehicule(entity.getIdVehicule())
                .idVoyage(entity.getIdVoyage())
                .idAgenceVoyage(entity.getIdAgenceVoyage())
                .idChauffeur(entity.getIdChauffeur())
                .build();
    }

    public LigneVoyageEntity toEntity(LigneVoyage domain) {
        if (domain == null) return null;

        return LigneVoyageEntity.builder()
                .idLigneVoyage(domain.getIdLigneVoyage())
                .idClassVoyage(domain.getIdClassVoyage())
                .idVehicule(domain.getIdVehicule())
                .idVoyage(domain.getIdVoyage())
                .idAgenceVoyage(domain.getIdAgenceVoyage())
                .idChauffeur(domain.getIdChauffeur())
                .build();
    }
}

