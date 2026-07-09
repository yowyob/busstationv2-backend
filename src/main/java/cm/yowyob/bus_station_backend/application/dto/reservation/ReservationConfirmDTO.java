package cm.yowyob.bus_station_backend.application.dto.reservation;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReservationConfirmDTO {
    private UUID idReservation;
    private double montantPaye;
}