package cm.yowyob.bus_station_backend.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SoldeIndemnisation {
    private UUID idSolde;
    private double solde;
    private String type;
    private UUID idUser;
    private UUID idAgenceVoyage;
}
