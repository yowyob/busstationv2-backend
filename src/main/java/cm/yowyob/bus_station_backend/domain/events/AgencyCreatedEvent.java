package cm.yowyob.bus_station_backend.domain.events;

import cm.yowyob.bus_station_backend.domain.model.AgenceVoyage;
import cm.yowyob.bus_station_backend.domain.model.User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
public class AgencyCreatedEvent {
    private final AgenceVoyage agence;
    private final User manager;
    private final LocalDateTime occurredOn = LocalDateTime.now();
}
