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
public class ReservationDTO {
    private int nbrPassager;
    private double montantPaye;
    private UUID idUser;
    private UUID idVoyage;
    private PassagerDTO[] passagerDTO;
}