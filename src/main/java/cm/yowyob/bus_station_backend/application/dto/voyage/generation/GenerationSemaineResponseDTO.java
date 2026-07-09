package cm.yowyob.bus_station_backend.application.dto.voyage.generation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerationSemaineResponseDTO {
    private List<UUID> voyagesPubliesIds;
    private List<UUID> brouillonsCreesIds;
    private List<String> erreurs;
    private int totalPublie;
    private int totalBrouillons;
}