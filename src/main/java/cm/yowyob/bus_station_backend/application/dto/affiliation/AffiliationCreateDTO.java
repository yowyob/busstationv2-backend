package cm.yowyob.bus_station_backend.application.dto.affiliation;

import java.time.LocalDate;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AffiliationCreateDTO {

    @NotNull
    private UUID gareRoutiereId;

    @NotNull
    private UUID agencyId;

    /**
     * Optionnel : si null, le service ira chercher le longName de l'agence.
     */
    private String agencyName;

    /**
     * Optionnel : si null, calculé depuis les TAXES de la gare.
     */
    private Double montantAffiliation;

    /**
     * Optionnel : si null, par défaut +1 an.
     */
    private LocalDate echeance;
}
