package cm.yowyob.bus_station_backend.application.dto.reservation;

import cm.yowyob.bus_station_backend.domain.enums.StatutVoyage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Duration;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class BilletDTO {
    private String titre;
    private String description;
    private LocalDateTime dateDepartPrev;
    private String lieuDepart;
    private LocalDateTime dateDepartEffectif;
    private LocalDateTime dateArriveEffectif;
    private String lieuArrive;
    private LocalDateTime heureDepartEffectif;
    private Duration dureeVoyage;
    private LocalDateTime heureArrive;
    private StatutVoyage statusVoyage;
    private String smallImage;
    private String bigImage;
    private String nomClasseVoyage;
    private double prix;
    private String nomAgence;
    private String pointDeDepart;
    private String pointArrivee;
    String numeroPieceIdentific;
    String nom;
    String genre;
    int age;
    int nbrBaggage;
    int placeChoisis;
}
