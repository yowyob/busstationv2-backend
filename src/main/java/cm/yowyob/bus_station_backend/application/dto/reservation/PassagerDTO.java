package cm.yowyob.bus_station_backend.application.dto.reservation;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PassagerDTO {
    String numeroPieceIdentific;
    String nom;
    String genre;
    int age;
    int nbrBaggage;
    int placeChoisis;
}