package cm.yowyob.bus_station_backend.application.dto.voyage.generation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchingPreviewResponseDTO {
    private List<MatchingPreviewItemDTO> voyagesPreview;
    private int totalPublie;
    private int totalIncomplet;
    private int totalIgnore;  // créneaux sans jourSemaine matchant la semaine
}