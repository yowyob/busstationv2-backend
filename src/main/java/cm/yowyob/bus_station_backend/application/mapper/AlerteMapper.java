package cm.yowyob.bus_station_backend.application.mapper;

import cm.yowyob.bus_station_backend.application.dto.alerte.AlerteResponseDTO;
import cm.yowyob.bus_station_backend.domain.model.AlerteAgence;
import org.springframework.stereotype.Component;

@Component
public class AlerteMapper {

    public AlerteResponseDTO toResponse(AlerteAgence domain) {
        if (domain == null) return null;

        return AlerteResponseDTO.builder()
                .idAlerte(domain.getIdAlerte())
                .gareId(domain.getGareId())
                .agenceId(domain.getAgenceId())
                .bsmId(domain.getBsmId())
                .type(domain.getType())
                .message(domain.getMessage())
                .isLu(domain.isLu())
                .createdAt(domain.getCreatedAt())
                .luAt(domain.getLuAt())
                .build();
    }
}