package cm.yowyob.bus_station_backend.application.dto.affiliation;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO PATCH-like pour `PUT /affiliation/{id}`. Tous les champs sont nullables.
 * Seuls les champs non-null sont mis à jour.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AffiliationUpdateDTO {
    private String agencyName;
    private Double montantAffiliation;
    private LocalDate echeance;
}
