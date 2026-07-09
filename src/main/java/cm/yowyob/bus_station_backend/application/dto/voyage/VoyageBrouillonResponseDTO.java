package cm.yowyob.bus_station_backend.application.dto.voyage;

import cm.yowyob.bus_station_backend.domain.enums.StatutBrouillon;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoyageBrouillonResponseDTO {
    private UUID id;
    private UUID agenceVoyageId;
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

    private StatutBrouillon statutBrouillon;
    private String notes;

    private UUID voyageId;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}