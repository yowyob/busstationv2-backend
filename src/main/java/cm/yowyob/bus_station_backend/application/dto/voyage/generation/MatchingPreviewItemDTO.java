package cm.yowyob.bus_station_backend.application.dto.voyage.generation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchingPreviewItemDTO {
    private UUID ligneServiceId;
    private String titre;
    private String lieuDepart;
    private String lieuArrive;
    private LocalDateTime dateDepartPrev;
    private LocalDateTime heureArrive;

    private UUID vehiculeMatcheId;
    private UUID chauffeurMatcheId;
    private UUID classVoyageId;

    /** PUBLIE | INCOMPLET */
    private String statutPrevu;

    private List<String> conflits;
}