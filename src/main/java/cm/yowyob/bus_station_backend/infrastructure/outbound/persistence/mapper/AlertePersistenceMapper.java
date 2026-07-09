package cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.mapper;

import cm.yowyob.bus_station_backend.domain.enums.TypeAlerte;
import cm.yowyob.bus_station_backend.domain.model.AlerteAgence;
import cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.entity.AlerteAgenceEntity;
import org.springframework.stereotype.Component;

@Component
public class AlertePersistenceMapper {

    public AlerteAgenceEntity toEntity(AlerteAgence domain) {
        if (domain == null) return null;

        return AlerteAgenceEntity.builder()
                .idAlerte(domain.getIdAlerte())
                .gareId(domain.getGareId())
                .agenceId(domain.getAgenceId())
                .bsmId(domain.getBsmId())
                .type(domain.getType() != null ? domain.getType().name() : null)
                .message(domain.getMessage())
                .isLu(domain.isLu())
                .createdAt(domain.getCreatedAt())
                .luAt(domain.getLuAt())
                .build();
    }

    public AlerteAgence toDomain(AlerteAgenceEntity entity) {
        if (entity == null) return null;

        return AlerteAgence.builder()
                .idAlerte(entity.getIdAlerte())
                .gareId(entity.getGareId())
                .agenceId(entity.getAgenceId())
                .bsmId(entity.getBsmId())
                .type(entity.getType() != null ? TypeAlerte.valueOf(entity.getType()) : null)
                .message(entity.getMessage())
                .isLu(entity.isLu())
                .createdAt(entity.getCreatedAt())
                .luAt(entity.getLuAt())
                .build();
    }
}
