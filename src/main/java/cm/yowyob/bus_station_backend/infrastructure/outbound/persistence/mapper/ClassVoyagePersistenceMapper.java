package cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.mapper;
import cm.yowyob.bus_station_backend.domain.model.ClassVoyage;
import cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.entity.ClassVoyageEntity;
import org.springframework.stereotype.Component;

@Component
public class ClassVoyagePersistenceMapper {
    public ClassVoyage toDomain(ClassVoyageEntity entity) {
        if (entity == null) return null;
        return ClassVoyage.builder()
                .idClassVoyage(entity.getId())
                .nom(entity.getNom())
                .prix(entity.getPrix())
                .idAgenceVoyage(entity.getIdAgenceVoyage())
                .build();
    }
    public ClassVoyageEntity toEntity(ClassVoyage domain) {
        if (domain == null) return null;
        return ClassVoyageEntity.builder()
                .id(domain.getIdClassVoyage())
                .nom(domain.getNom())
                .prix(domain.getPrix())
                .idAgenceVoyage(domain.getIdAgenceVoyage())
                .isActive(true)
                .version(1)
                .build();
    }
}
