package cm.yowyob.bus_station_backend.application.dto.voyage.generation;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GenerationUnitaireRequestDTO {
    @NotNull(message = "ligneServiceId est obligatoire")
    private UUID ligneServiceId;

    @NotNull(message = "dateDepartPrev est obligatoire (format YYYY-MM-DD)")
    private LocalDate dateDepartPrev;

    /**
     * Si true ET matching réussi → voyage PUBLIE.
     * Si false ou matching INCOMPLET → VoyageBrouillon créé.
     */
    private boolean publierDirectement;
}