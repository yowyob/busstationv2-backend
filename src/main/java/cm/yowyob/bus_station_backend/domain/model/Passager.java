package cm.yowyob.bus_station_backend.domain.model;

import cm.yowyob.bus_station_backend.domain.enums.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Passager {
    private UUID idPassager;
    private String numeroPieceIdentific;
    private String nom;
    private Gender genre;
    private int age;
    private int nbrBaggage;
    private UUID idReservation;
    private int placeChoisis;
}
