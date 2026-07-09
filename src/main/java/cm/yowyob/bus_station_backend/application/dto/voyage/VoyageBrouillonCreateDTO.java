package cm.yowyob.bus_station_backend.application.dto.voyage;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class VoyageBrouillonCreateDTO {
    @NotNull(message = "L'agence est obligatoire")
    private UUID agenceVoyageId;

    private UUID ligneServiceId;

    @NotBlank(message = "Le titre est obligatoire")
    private String titre;

    private String description;

    @NotBlank(message = "Le lieu de départ est obligatoire")
    private String lieuDepart;

    @NotBlank(message = "Le lieu d'arrivée est obligatoire")
    private String lieuArrive;

    private String pointDeDepart;
    private String pointArrivee;

    private LocalDateTime dateDepartPrev;
    private LocalDateTime heureDepartEffectif;
    private LocalDateTime heureArrive;
    private String dureeEstimee;

    private UUID classVoyageId;
    private UUID vehiculeId;
    private UUID chauffeurId;

    private Integer nbrPlaceReservable;
    private Double prix;

    private List<String> amenities;
    private String smallImage;
    private String bigImage;

    private LocalDateTime dateLimiteReservation;
    private LocalDateTime dateLimiteConfirmation;

    private String notes;
}