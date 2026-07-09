package cm.yowyob.bus_station_backend.application.dto.voyage;

import cm.yowyob.bus_station_backend.domain.enums.Amenities;
import cm.yowyob.bus_station_backend.domain.enums.StatutVoyage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
// ce dto est là pour la liste des voyages
public class VoyagePreviewDTO {
    private UUID idVoyage;
    private String nomAgence;
    private String lieuDepart;
    private String lieuArrive;
    private int nbrPlaceRestante;
    private int nbrPlaceReservable;
    private LocalDateTime dateDepartPrev;
    private Duration dureeVoyage;
    private String nomClasseVoyage;
    private double prix;
    private String smallImage;
    private String bigImage;
    private List<Amenities> amenities;
    private StatutVoyage statusVoyage;

}