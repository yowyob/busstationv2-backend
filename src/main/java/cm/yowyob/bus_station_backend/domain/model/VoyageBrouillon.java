package cm.yowyob.bus_station_backend.domain.model;

import cm.yowyob.bus_station_backend.domain.enums.StatutBrouillon;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoyageBrouillon {
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
    private String amenities; // CSV

    private String smallImage;
    private String bigImage;

    private LocalDateTime dateLimiteReservation;
    private LocalDateTime dateLimiteConfirmation;

    private StatutBrouillon statutBrouillon;
    private String notes;

    private UUID voyageId; // ID du voyage créé après publication (traçabilité)

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Détermine si toutes les ressources nécessaires à une publication sont présentes.
     * Utilisé pour calculer automatiquement INCOMPLET vs PRET.
     */
    public boolean isComplet() {
        return titre != null && !titre.isBlank()
                && lieuDepart != null && !lieuDepart.isBlank()
                && lieuArrive != null && !lieuArrive.isBlank()
                && pointDeDepart != null && !pointDeDepart.isBlank()
                && pointArrivee != null && !pointArrivee.isBlank()
                && dateDepartPrev != null
                && heureArrive != null
                && classVoyageId != null
                && vehiculeId != null
                && chauffeurId != null
                && nbrPlaceReservable != null && nbrPlaceReservable > 0
                && prix != null && prix > 0
                && dateLimiteReservation != null
                && dateLimiteConfirmation != null;
    }
}