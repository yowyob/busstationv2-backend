package cm.yowyob.bus_station_backend.application.dto.affiliation;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import cm.yowyob.bus_station_backend.domain.enums.StatutTaxe;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AffiliationResponseDTO {
    private UUID id;
    private UUID gareRoutiereId;
    private UUID agencyId;
    private String agencyName;
    private StatutTaxe statut;
    private LocalDate echeance;
    private Double montantAffiliation;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
