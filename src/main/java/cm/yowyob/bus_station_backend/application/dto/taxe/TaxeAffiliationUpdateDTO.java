package cm.yowyob.bus_station_backend.application.dto.taxe;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * PATCH-like pour `PUT /taxe-affiliation/{id}`. Champs nullables.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaxeAffiliationUpdateDTO {
    private String nomTaxe;
    private String description;
    private Double tauxTaxe;
    private Double montantFixe;
    private LocalDate dateEffet;
}
