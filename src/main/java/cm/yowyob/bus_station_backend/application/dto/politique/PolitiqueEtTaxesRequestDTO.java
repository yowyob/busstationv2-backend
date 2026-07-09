package cm.yowyob.bus_station_backend.application.dto.politique;

import cm.yowyob.bus_station_backend.domain.enums.PolitiqueOuTaxe;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PolitiqueEtTaxesRequestDTO {
    private UUID gareRoutiereId;
    private String nomPolitique;
    private String description;
    private Double tauxTaxe;
    private Double montantFixe;
    private LocalDate dateEffet;
    private PolitiqueOuTaxe type;
}