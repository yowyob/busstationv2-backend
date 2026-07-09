package cm.yowyob.bus_station_backend.application.dto.reservation;

import cm.yowyob.bus_station_backend.domain.model.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ReservationDetailDTO {
    private Reservation reservation;

    public ReservationDetailDTO(Reservation reservation) {
        this.reservation = reservation;
    }

    private List<Passager> passager;
    private Voyage voyage;
    private AgenceVoyage agence;
    private Vehicule vehicule;
}
