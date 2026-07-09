package cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.mapper;

import cm.yowyob.bus_station_backend.domain.model.PlanningVoyage;
import cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.entity.PlanningVoyageEntity;
import org.springframework.stereotype.Component;

@Component
public class PlanningVoyagePersistenceMapper {

    public PlanningVoyageEntity toEntity(PlanningVoyage domain) {
        PlanningVoyageEntity entity = PlanningVoyageEntity.builder()
                .idPlanning(domain.getIdPlanning())
                .idAgenceVoyage(domain.getIdAgenceVoyage())
                .nom(domain.getNom())
                .description(domain.getDescription())
                .recurrence(domain.getRecurrence())
                .statut(domain.getStatut())
                .dateDebut(domain.getDateDebut())
                .dateFin(domain.getDateFin())
                .dateCreation(domain.getDateCreation())
                .dateModification(domain.getDateModification())
                .build();
        return entity;
    }

    public PlanningVoyageEntity toNewEntity(PlanningVoyage domain) {
        PlanningVoyageEntity entity = toEntity(domain);
        entity.setAsNew();
        return entity;
    }

    public PlanningVoyage toDomain(PlanningVoyageEntity entity) {
        return PlanningVoyage.builder()
                .idPlanning(entity.getIdPlanning())
                .idAgenceVoyage(entity.getIdAgenceVoyage())
                .nom(entity.getNom())
                .description(entity.getDescription())
                .recurrence(entity.getRecurrence())
                .statut(entity.getStatut())
                .dateDebut(entity.getDateDebut())
                .dateFin(entity.getDateFin())
                .dateCreation(entity.getDateCreation())
                .dateModification(entity.getDateModification())
                .build();
    }
}
