package cm.yowyob.bus_station_backend.domain.events;

import cm.yowyob.bus_station_backend.domain.model.Reservation;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
public class ReservationCancelledEvent {
    private final Reservation reservation;
    private final String reason;
    private final double refundAmount;
    private final LocalDateTime occurredOn = LocalDateTime.now();
}
