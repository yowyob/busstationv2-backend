package cm.yowyob.bus_station_backend.domain.events;

import cm.yowyob.bus_station_backend.domain.model.User;
import cm.yowyob.bus_station_backend.domain.model.Voyage;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
public class DriverAssignedEvent {
    private final User driver;
    private final Voyage voyage;
    private final LocalDateTime occurredOn = LocalDateTime.now();
}