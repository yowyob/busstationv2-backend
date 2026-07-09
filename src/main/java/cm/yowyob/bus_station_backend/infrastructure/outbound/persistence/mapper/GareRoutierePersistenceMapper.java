package cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.mapper;

import cm.yowyob.bus_station_backend.domain.enums.ServicesGareRoutiere;
import cm.yowyob.bus_station_backend.domain.model.GareRoutiere;
import cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.entity.GareRoutiereEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class GareRoutierePersistenceMapper {

    public GareRoutiereEntity toEntity(GareRoutiere domain) {
        if (domain == null) return null;
        return GareRoutiereEntity.builder()
                .idGareRoutiere(domain.getIdGareRoutiere())
                .nomGareRoutiere(domain.getNomGareRoutiere())
                .adresse(domain.getAdresse())
                .ville(domain.getVille())
                .quartier(domain.getQuartier())
                .description(domain.getDescription())
                .services(domain.getServices() == null ? null : domain.getServices().stream().map(Enum::name).collect(Collectors.joining(",")))
                .horaires(domain.getHoraires())
                .photoUrl(domain.getPhotoUrl())
                .nomPresident(domain.getNomPresident())
                .idCoordonneeGPS(domain.getIdCoordonneeGPS())
                .managerId(domain.getManagerId())
                .build();
    }

    public GareRoutiere toDomain(GareRoutiereEntity entity) {
        if (entity == null) return null;
        GareRoutiere gareRoutiere = new GareRoutiere();
        gareRoutiere.setIdGareRoutiere(entity.getIdGareRoutiere());
        gareRoutiere.setNomGareRoutiere(entity.getNomGareRoutiere());
        gareRoutiere.setAdresse(entity.getAdresse());
        gareRoutiere.setVille(entity.getVille());
        gareRoutiere.setQuartier(entity.getQuartier());
        gareRoutiere.setDescription(entity.getDescription());
        gareRoutiere.setServices(entity.getServices() == null || entity.getServices().isEmpty() ? List.of() : List.of(entity.getServices().split(",")).stream().map(ServicesGareRoutiere::valueOf).toList());
        gareRoutiere.setHoraires(entity.getHoraires());
        gareRoutiere.setPhotoUrl(entity.getPhotoUrl());
        gareRoutiere.setNomPresident(entity.getNomPresident());
        gareRoutiere.setIdCoordonneeGPS(entity.getIdCoordonneeGPS());
        gareRoutiere.setManagerId(entity.getManagerId());
        return gareRoutiere;
    }
}

