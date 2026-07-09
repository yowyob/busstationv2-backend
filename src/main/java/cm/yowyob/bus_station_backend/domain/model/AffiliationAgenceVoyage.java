package cm.yowyob.bus_station_backend.domain.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import cm.yowyob.bus_station_backend.domain.enums.StatutTaxe;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AffiliationAgenceVoyage {
  private UUID id;
  private UUID agencyId;
  private String agencyName;
  private UUID gareRoutiereId;

  private StatutTaxe statut;
  private LocalDate echeance;
  private Double montantAffiliation;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
