package cm.yowyob.bus_station_backend.domain.events;

import cm.yowyob.bus_station_backend.domain.model.Voyage;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
public class VoyageCancelledEvent {
    private final Voyage voyage;
    private final String reason;
    private final LocalDateTime occurredOn = LocalDateTime.now();
}
