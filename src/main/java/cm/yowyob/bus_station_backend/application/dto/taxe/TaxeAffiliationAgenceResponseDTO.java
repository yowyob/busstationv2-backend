package cm.yowyob.bus_station_backend.application.dto.taxe;

import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Résumé des taxes dues par une agence : on agrège toutes les taxes de la gare
 * à laquelle l'agence est affiliée.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaxeAffiliationAgenceResponseDTO {
    private UUID agencyId;
    private UUID gareRoutiereId;
    private Double montantTotalDu;
    private List<TaxeAffiliationResponseDTO> taxes;
}
