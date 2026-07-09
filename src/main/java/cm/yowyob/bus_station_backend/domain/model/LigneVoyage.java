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
public class LigneVoyage {
    private UUID idLigneVoyage;
    private UUID idClassVoyage;
    private UUID idVehicule;
    private UUID idVoyage;
    private UUID idAgenceVoyage;
    private UUID idChauffeur;
}
