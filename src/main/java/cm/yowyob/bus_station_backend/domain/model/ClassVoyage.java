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
public class ClassVoyage {
    private UUID idClassVoyage;
    private String nom;
    private double prix;
    private double tauxAnnulation;
    private UUID idAgenceVoyage;
}
