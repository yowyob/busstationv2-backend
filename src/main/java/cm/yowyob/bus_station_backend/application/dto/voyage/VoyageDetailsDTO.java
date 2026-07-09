package cm.yowyob.bus_station_backend.application.dto.voyage;

import cm.yowyob.bus_station_backend.application.dto.user.UserResponseDTO;
import cm.yowyob.bus_station_backend.domain.enums.Amenities;
import cm.yowyob.bus_station_backend.domain.enums.StatutVoyage;
import cm.yowyob.bus_station_backend.domain.model.Vehicule;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class VoyageDetailsDTO {
    private UUID idVoyage;
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
    private int nbrPlaceReservable;
    private int nbrPlaceRestante;
    private LocalDateTime datePublication;
    private LocalDateTime dateLimiteReservation;
    private LocalDateTime dateLimiteConfirmation;
    private StatutVoyage statusVoyage;
    private String smallImage;
    private String bigImage;
    private String nomClasseVoyage;
    private double prix;
    private String nomAgence;
    private String pointDeDepart;
    private String pointArrivee;
    private Vehicule vehicule;
    private UserResponseDTO chauffeur;
    private List<Integer> placeReservees;
    private List<Amenities> amenities;
}