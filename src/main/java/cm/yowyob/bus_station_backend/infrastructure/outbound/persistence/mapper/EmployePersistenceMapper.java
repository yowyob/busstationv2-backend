package cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.mapper;

import cm.yowyob.bus_station_backend.domain.model.EmployeAgenceVoyage;
import cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.entity.EmployeEntity;
import org.springframework.stereotype.Component;

@Component
public class EmployePersistenceMapper {

    public EmployeAgenceVoyage toDomain(EmployeEntity entity) {
        if (entity == null) return null;

        return EmployeAgenceVoyage.builder()
                .employeId(entity.getEmployeId())
                .agenceVoyageId(entity.getAgenceVoyageId())
                .userId(entity.getUserId())
                .poste(entity.getPoste())
                .dateEmbauche(entity.getDateEmbauche())
                .dateFinContrat(entity.getDateFinContrat())
                .statutEmploye(entity.getStatutEmploye())
                .salaire(entity.getSalaire())
                .departement(entity.getDepartement())
                .managerId(entity.getManagerId())
                .build();
    }

    public EmployeEntity toEntity(EmployeAgenceVoyage domain) {
        if (domain == null) return null;

        return EmployeEntity.builder()
                .employeId(domain.getEmployeId())
                .agenceVoyageId(domain.getAgenceVoyageId())
                .userId(domain.getUserId())
                .poste(domain.getPoste())
                .dateEmbauche(domain.getDateEmbauche())
                .dateFinContrat(domain.getDateFinContrat())
                .statutEmploye(domain.getStatutEmploye())
                .salaire(domain.getSalaire())
                .departement(domain.getDepartement())
                .managerId(domain.getManagerId())
                .build();
    }
}
