package cm.yowyob.bus_station_backend.application.dto.politiquegare;

import java.time.LocalDate;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PolitiqueGareResponseDTO {
    private UUID idPolitique;
    private UUID gareRoutiereId;
    private String titre;
    private String description;
    private Double montant;
    private LocalDate dateEffet;
    private String documentUrl;
}
