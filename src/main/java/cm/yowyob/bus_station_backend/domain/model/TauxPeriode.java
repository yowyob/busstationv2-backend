package cm.yowyob.bus_station_backend.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TauxPeriode {
    private UUID idTauxPeriode;
    private double valeur;
    private LocalDateTime dateDebut;
    private LocalDateTime dateFin;
    private UUID idPolitiqueAnnulation;
}
