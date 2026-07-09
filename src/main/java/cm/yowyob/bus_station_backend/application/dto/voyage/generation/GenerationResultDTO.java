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
public class GenerationResultDTO {
    private UUID ligneServiceId;
    private LocalDateTime dateDepartPrev;

    /** PUBLIE | INCOMPLET */
    private String statut;

    /** Si PUBLIE */
    private UUID voyageId;

    /** Si INCOMPLET ou si publierDirectement=false */
    private UUID brouillonId;

    /** Ressources retenues (peuvent être null si INCOMPLET) */
    private UUID vehiculeId;
    private UUID chauffeurId;
    private UUID classVoyageId;

    /** Liste des raisons pour lesquelles c'est INCOMPLET */
    private List<String> conflits;

    private String message;
}