package cm.yowyob.bus_station_backend.application.dto.taxe;

import java.time.LocalDate;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaxeAffiliationCreateDTO {

    @NotNull
    private UUID gareRoutiereId;

    @NotBlank
    private String nomTaxe;

    private String description;

    /**
     * Taux en %, ex: 0.05 pour 5%. Optionnel.
     */
    private Double tauxTaxe;

    /**
     * Montant fixe (FCFA). Optionnel mais l'un des deux doit être renseigné.
     */
    private Double montantFixe;

    private LocalDate dateEffet;
}
