package cm.yowyob.bus_station_backend.domain.model;

import java.time.LocalDate;
import java.util.UUID;

import cm.yowyob.bus_station_backend.domain.enums.PolitiqueOuTaxe;
import lombok.Data;

@Data
public class PolitiqueEtTaxes {
  private UUID idPolitique;
  private UUID gareRoutiereId;
  private String nomPolitique;
  private String description;
  private Double tauxTaxe; // En cas de taxe
  private Double montantFixe; // En cas de taxe fixe
  private LocalDate dateEffet;
  private String DocumentUrl; // Peut être null
  private PolitiqueOuTaxe type;

}
