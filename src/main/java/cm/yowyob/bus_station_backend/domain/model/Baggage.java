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
public class Baggage {
    private UUID idBaggage;
    private String nbreBaggage;
    private UUID idPassager;
}
