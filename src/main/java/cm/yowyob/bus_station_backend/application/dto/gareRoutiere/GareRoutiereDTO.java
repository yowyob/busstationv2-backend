package cm.yowyob.bus_station_backend.application.dto.gareRoutiere;

import cm.yowyob.bus_station_backend.domain.enums.ServicesGareRoutiere;
import cm.yowyob.bus_station_backend.domain.model.Coordonnee;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GareRoutiereDTO {

    private UUID idGareRoutiere;
    private String nomGareRoutiere;
    private String adresse;
    private String ville;
    private String quartier;
    private String description;
    private List<ServicesGareRoutiere> services;
    private String horaires;
    private String photoUrl;
    private String nomPresident;
    private UUID idCoordonneeGPS;
    private UUID managerId;
    private Integer nbreAgence;
    private Coordonnee localisation;
}
