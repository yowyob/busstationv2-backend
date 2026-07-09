package cm.yowyob.bus_station_backend.application.dto.planning;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Request DTO for generating actual Voyage instances from a planning template.
 * The agency manager specifies a date range and the system will create voyages
 * for each matching creneau within that range.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerateVoyagesFromPlanningDTO {

    @NotNull(message = "L'ID du planning est obligatoire")
    @JsonProperty("id_planning")
    private UUID idPlanning;

    @NotNull(message = "La date de début est obligatoire")
    @JsonProperty("date_debut")
    private LocalDate dateDebut;

    @NotNull(message = "La date de fin est obligatoire")
    @JsonProperty("date_fin")
    private LocalDate dateFin;
}
