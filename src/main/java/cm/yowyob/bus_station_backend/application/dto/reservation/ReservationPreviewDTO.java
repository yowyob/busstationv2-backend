package cm.yowyob.bus_station_backend.application.dto.reservation;

import cm.yowyob.bus_station_backend.domain.model.AgenceVoyage;
import cm.yowyob.bus_station_backend.domain.model.Reservation;
import cm.yowyob.bus_station_backend.domain.model.Voyage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ReservationPreviewDTO {
    Reservation reservation;
    Voyage voyage;
    AgenceVoyage agence;
}
