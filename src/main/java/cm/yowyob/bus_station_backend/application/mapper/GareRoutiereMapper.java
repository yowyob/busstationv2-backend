package cm.yowyob.bus_station_backend.application.mapper;

import cm.yowyob.bus_station_backend.application.dto.gareRoutiere.GareRoutiereDTO;
import cm.yowyob.bus_station_backend.application.dto.gareRoutiere.GareRoutierePreviewDTO;
import cm.yowyob.bus_station_backend.application.dto.gareRoutiere.GareRoutiereRequestDTO;
import cm.yowyob.bus_station_backend.domain.model.Coordonnee;
import cm.yowyob.bus_station_backend.domain.model.GareRoutiere;
import org.springframework.stereotype.Component;

@Component
public class GareRoutiereMapper {

    public GareRoutiere toDomain(GareRoutiereRequestDTO dto) {

        if (dto == null)
            return null;

        GareRoutiere gareRoutiere = new GareRoutiere();

        gareRoutiere.setManagerId(dto.getManagerId());
        gareRoutiere.setNomGareRoutiere(dto.getNomGareRoutiere());
        gareRoutiere.setAdresse(dto.getAdresse());
        gareRoutiere.setVille(dto.getVille());
        gareRoutiere.setQuartier(dto.getQuartier());
        gareRoutiere.setDescription(dto.getDescription());
        gareRoutiere.setServices(dto.getServices());
        gareRoutiere.setHoraires(dto.getHoraires());
        gareRoutiere.setNomPresident(dto.getNomPresident());
        gareRoutiere.setNbreAgence(0);

        return gareRoutiere;
    }

    public GareRoutiereDTO toDTO(GareRoutiere gareRoutiere, Coordonnee coordonnee) {

        if (gareRoutiere == null)
            return null;

        return GareRoutiereDTO.builder()
                .idGareRoutiere(gareRoutiere.getIdGareRoutiere())
                .nomGareRoutiere(gareRoutiere.getNomGareRoutiere())
                .adresse(gareRoutiere.getAdresse())
                .ville(gareRoutiere.getVille())
                .quartier(gareRoutiere.getQuartier())
                .description(gareRoutiere.getDescription())
                .services(gareRoutiere.getServices())
                .horaires(gareRoutiere.getHoraires())
                .photoUrl(gareRoutiere.getPhotoUrl())
                .nomPresident(gareRoutiere.getNomPresident())
                .idCoordonneeGPS(gareRoutiere.getIdCoordonneeGPS())
                .managerId(gareRoutiere.getManagerId())
                .nbreAgence(gareRoutiere.getNbreAgence())
                .localisation(coordonnee)
                .build();
    }

    public GareRoutierePreviewDTO toPreviewDTO(GareRoutiere gareRoutiere) {

        if (gareRoutiere == null)
            return null;

        GareRoutierePreviewDTO previewDTO = new GareRoutierePreviewDTO();

        previewDTO.setIdGareRoutiere(gareRoutiere.getIdGareRoutiere());
        previewDTO.setNomGareRoutiere(gareRoutiere.getNomGareRoutiere());
        previewDTO.setVille(gareRoutiere.getVille());
        previewDTO.setQuartier(gareRoutiere.getQuartier());
        previewDTO.setPhotoUrl(gareRoutiere.getPhotoUrl());
        previewDTO.setServices(gareRoutiere.getServices());
        // Assuming nbreAgence and isOpen are derived or set elsewhere
        previewDTO.setNbreAgence(gareRoutiere.getNbreAgence()); // Placeholder
        previewDTO.setOpen(true); // Placeholder

        return previewDTO;
    }
}
