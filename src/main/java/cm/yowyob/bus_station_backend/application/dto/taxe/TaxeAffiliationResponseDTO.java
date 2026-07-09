package cm.yowyob.bus_station_backend.application.dto.taxe;

import java.time.LocalDate;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaxeAffiliationResponseDTO {
    private UUID idTaxe;
    private UUID gareRoutiereId;
    private String nomTaxe;
    private String description;
    private Double tauxTaxe;
    private Double montantFixe;
    private LocalDate dateEffet;
    private String documentUrl;
}
