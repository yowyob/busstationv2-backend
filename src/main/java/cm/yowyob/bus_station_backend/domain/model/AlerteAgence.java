package cm.yowyob.bus_station_backend.domain.model;

import cm.yowyob.bus_station_backend.domain.enums.TypeAlerte;
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
public class AlerteAgence {

    private UUID        idAlerte;
    private UUID        gareId;
    private UUID        agenceId;
    private UUID        bsmId;
    private TypeAlerte  type;
    private String      message;
    private boolean     isLu;
    private LocalDateTime createdAt;
    private LocalDateTime luAt;
}