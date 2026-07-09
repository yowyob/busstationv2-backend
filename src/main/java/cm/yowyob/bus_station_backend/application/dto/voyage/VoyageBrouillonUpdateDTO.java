package cm.yowyob.bus_station_backend.application.dto.voyage;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Tous les champs sont nullables : seuls les non-null seront appliqués (PATCH-like).
 */
@Data
public class VoyageBrouillonUpdateDTO {
    private UUID ligneServiceId;
    private String titre;
    private String description;
    private String lieuDepart;
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