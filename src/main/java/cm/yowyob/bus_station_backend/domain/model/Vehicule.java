package cm.yowyob.bus_station_backend.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Vehicule {
    private UUID idVehicule;
    private String nom;
    private String modele;
    private String description;
    private int nbrPlaces;
    private String PlaqueMatricule;
    private String lienPhoto;
    private UUID idAgenceVoyage;
}
