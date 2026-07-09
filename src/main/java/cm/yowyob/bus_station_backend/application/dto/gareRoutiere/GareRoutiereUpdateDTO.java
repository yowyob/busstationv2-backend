// src/main/java/cm/yowyob/bus_station_backend/application/dto/gareRoutiere/GareRoutiereUpdateDTO.java

package cm.yowyob.bus_station_backend.application.dto.gareRoutiere;

import cm.yowyob.bus_station_backend.domain.enums.ServicesGareRoutiere;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Payload PATCH-like pour PUT /gare/{gareId}.
 * Tous les champs sont optionnels : null = ignoré.
 * Les champs sensibles (managerId, idCoordonneeGPS) ne sont pas modifiables ici.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GareRoutiereUpdateDTO {
    private String nomGareRoutiere;
    private String adresse;
    private String ville;
    private String quartier;
    private String description;
    private List<ServicesGareRoutiere> services;
    private String horaires;
    private String photoUrl;
    private String nomPresident;
}