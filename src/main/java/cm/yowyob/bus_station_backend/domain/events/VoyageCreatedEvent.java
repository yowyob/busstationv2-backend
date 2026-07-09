package cm.yowyob.bus_station_backend.domain.events;

import cm.yowyob.bus_station_backend.domain.model.Voyage;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@RequiredArgsConstructor
public class VoyageCreatedEvent {
    private final Voyage voyage;
    private final UUID agenceId;
    private final LocalDateTime occurredOn = LocalDateTime.now();
}
