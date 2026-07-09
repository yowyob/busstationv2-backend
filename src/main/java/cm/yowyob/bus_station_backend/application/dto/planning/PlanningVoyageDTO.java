package cm.yowyob.bus_station_backend.application.dto.planning;

import cm.yowyob.bus_station_backend.domain.enums.planning.RecurrenceType;
import cm.yowyob.bus_station_backend.domain.enums.planning.StatutPlanning;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanningVoyageDTO {

    @JsonProperty("id_planning")
    private UUID idPlanning;

    @NotNull(message = "L'ID de l'agence est obligatoire")
    @JsonProperty("id_agence_voyage")
    private UUID idAgenceVoyage;

    @NotBlank(message = "Le nom du planning est obligatoire")
    private String nom;

    private String description;

    @NotNull(message = "Le type de récurrence est obligatoire")
    private RecurrenceType recurrence;

    private StatutPlanning statut;

    @NotNull(message = "La date de début est obligatoire")
    @JsonProperty("date_debut")
    private LocalDate dateDebut;

    @JsonProperty("date_fin")
    private LocalDate dateFin;

    @JsonProperty("date_creation")
    private LocalDateTime dateCreation;

    @JsonProperty("date_modification")
    private LocalDateTime dateModification;

    private List<CreneauPlanningDTO> creneaux;
}