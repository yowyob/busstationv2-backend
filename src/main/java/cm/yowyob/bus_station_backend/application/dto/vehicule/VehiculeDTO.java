package cm.yowyob.bus_station_backend.application.dto.vehicule;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class VehiculeDTO {
    private UUID idVehicule;
    private String nom;
    private String modele;
    private String description;
    private int nbrPlaces;
    private String plaqueMatricule;
    private String lienPhoto;
    private UUID idAgenceVoyage;
}