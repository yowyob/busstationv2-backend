package cm.yowyob.bus_station_backend.domain.events;

import cm.yowyob.bus_station_backend.domain.enums.StatutPayment;
import cm.yowyob.bus_station_backend.domain.model.Reservation;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
public class PaymentProcessedEvent {
    private final Reservation reservation;
    private final StatutPayment status;
    private final String transactionId;
    private final LocalDateTime occurredOn = LocalDateTime.now();

    public boolean isSuccess() {
        return status == StatutPayment.PAID;
    }
}
