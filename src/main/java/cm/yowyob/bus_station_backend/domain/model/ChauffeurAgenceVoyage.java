package cm.yowyob.bus_station_backend.domain.model;

import cm.yowyob.bus_station_backend.domain.enums.StatutChauffeur;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChauffeurAgenceVoyage {
    private UUID chauffeurId;
    private UUID agenceVoyageId;
    private UUID userId;
    private StatutChauffeur statusChauffeur;
}
