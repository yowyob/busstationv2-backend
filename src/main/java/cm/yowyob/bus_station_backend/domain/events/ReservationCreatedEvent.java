package cm.yowyob.bus_station_backend.domain.events;

import cm.yowyob.bus_station_backend.domain.model.Reservation;
import cm.yowyob.bus_station_backend.domain.model.Voyage;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@RequiredArgsConstructor
public class ReservationCreatedEvent {
    private final Reservation reservation;
    private final Voyage voyage;
    private final UUID agenceId; // Pour notifier l'agence
    private final LocalDateTime occurredOn = LocalDateTime.now();
}
