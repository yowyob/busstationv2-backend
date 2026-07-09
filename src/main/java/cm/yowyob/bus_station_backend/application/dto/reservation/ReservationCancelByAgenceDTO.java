package cm.yowyob.bus_station_backend.application.dto.reservation;

import cm.yowyob.bus_station_backend.domain.model.Voyage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReservationCancelByAgenceDTO {
    private String causeAnnulation;
    private String origineAnnulation;
    private UUID idReservation;
    private Voyage voyage;
    private boolean canceled;
}
