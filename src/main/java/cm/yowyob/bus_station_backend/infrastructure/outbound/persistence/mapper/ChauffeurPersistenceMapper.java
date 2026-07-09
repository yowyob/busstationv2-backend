package cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.mapper;

import cm.yowyob.bus_station_backend.domain.model.ChauffeurAgenceVoyage;
import cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.entity.ChauffeurEntity;
import org.springframework.stereotype.Component;

@Component
public class ChauffeurPersistenceMapper {

    public ChauffeurAgenceVoyage toDomain(ChauffeurEntity entity) {
        if (entity == null) return null;

        return ChauffeurAgenceVoyage.builder()
                .chauffeurId(entity.getChauffeurId())
                .agenceVoyageId(entity.getAgenceVoyageId())
                .userId(entity.getUserId())
                .statusChauffeur(entity.getStatusChauffeur())
                .build();
    }

    public ChauffeurEntity toEntity(ChauffeurAgenceVoyage domain) {
        if (domain == null) return null;

        return ChauffeurEntity.builder()
                .chauffeurId(domain.getChauffeurId())
                .agenceVoyageId(domain.getAgenceVoyageId())
                .userId(domain.getUserId())
                .statusChauffeur(domain.getStatusChauffeur())
                .build();
    }
}

