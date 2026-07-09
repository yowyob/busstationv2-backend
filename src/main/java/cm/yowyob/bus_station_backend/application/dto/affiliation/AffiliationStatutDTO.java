package cm.yowyob.bus_station_backend.application.dto.affiliation;

import cm.yowyob.bus_station_backend.domain.enums.StatutTaxe;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO pour `PUT /affiliation/{id}/statut`.
 * StatutTaxe = { PAYE, EN_ATTENTE, EN_RETARD }.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AffiliationStatutDTO {

    @NotNull
    private StatutTaxe statut;
}
