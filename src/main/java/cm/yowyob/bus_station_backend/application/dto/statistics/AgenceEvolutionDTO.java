package cm.yowyob.bus_station_backend.application.dto.statistics;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AgenceEvolutionDTO {
    private List<EvolutionData> evolutionReservations;
    private List<EvolutionData> evolutionVoyages;
    private List<EvolutionData> evolutionRevenus;
    private List<EvolutionData> evolutionUtilisateurs;
}
