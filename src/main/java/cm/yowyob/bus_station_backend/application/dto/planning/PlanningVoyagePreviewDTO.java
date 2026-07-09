package cm.yowyob.bus_station_backend.application.dto.planning;

import cm.yowyob.bus_station_backend.domain.enums.planning.RecurrenceType;
import cm.yowyob.bus_station_backend.domain.enums.planning.StatutPlanning;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanningVoyagePreviewDTO {

    @JsonProperty("id_planning")
    private UUID idPlanning;

    private String nom;
    private String description;
    private RecurrenceType recurrence;
    private StatutPlanning statut;

    @JsonProperty("date_debut")
    private LocalDate dateDebut;

    @JsonProperty("date_fin")
    private LocalDate dateFin;

    @JsonProperty("nombre_creneaux")
    private int nombreCreneaux;

    @JsonProperty("nom_agence")
    private String nomAgence;
}
