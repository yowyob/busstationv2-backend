package cm.yowyob.bus_station_backend.application.dto.disponibilite;

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
public class DisponibiliteResponseDTO {
    private UUID resourceId;        // vehiculeId ou chauffeurId
    private String resourceType;    // "VEHICULE" ou "CHAUFFEUR"
    private String date;            // YYYY-MM-DD
    private String heure;           // HH:mm (peut être null)
    private boolean available;
    private List<UUID> conflictingVoyageIds;
    private String message;
}