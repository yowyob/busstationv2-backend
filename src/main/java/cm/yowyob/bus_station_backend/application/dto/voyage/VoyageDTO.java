package cm.yowyob.bus_station_backend.application.dto.voyage;

import cm.yowyob.bus_station_backend.domain.enums.Amenities;
import cm.yowyob.bus_station_backend.domain.enums.StatutVoyage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class VoyageDTO {
    private String titre;
    private String description;
    private LocalDateTime dateDepartPrev;
    private String lieuDepart;
    private String lieuArrive;
    private LocalDateTime heureDepartEffectif;
    private Duration dureeVoyage;
    private LocalDateTime heureArrive;
    private LocalDateTime datePublication;
    private LocalDateTime dateLimiteReservation;
    private LocalDateTime dateLimiteConfirmation;
    private StatutVoyage statusVoyage;
    private String smallImage;
    private String bigImage;
    private List<Amenities> amenities;

}

