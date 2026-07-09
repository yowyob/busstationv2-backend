package cm.yowyob.bus_station_backend.application.dto.statistic;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EvolutionData {
    private LocalDate date;
    private long valeur;
    private double montant; // Pour les revenus
}
