package cm.yowyob.bus_station_backend.application.dto.voyage;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class VoyageCancelDTO {
    private String causeAnnulation;
    private String origineAnnulation;

    @NotNull(message = "Id de l'agence de voyage requise")
    private UUID AgenceVoyageId;

    private UUID IdVoyage;
    private boolean canceled;
}
