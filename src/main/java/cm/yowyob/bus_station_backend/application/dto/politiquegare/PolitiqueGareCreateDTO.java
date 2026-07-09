package cm.yowyob.bus_station_backend.application.dto.politiquegare;

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
public class PolitiqueGareCreateDTO {

    @NotNull
    private UUID gareRoutiereId;

    @NotBlank
    private String titre;

    private String description;

    /**
     * Optionnel : pour les politiques type frais de service ou catégorie POLITIQUE
     * pouvant porter un montant indicatif.
     */
    private Double montant;

    private LocalDate dateEffet;
}
