package cm.yowyob.bus_station_backend.application.dto.voyage;

import cm.yowyob.bus_station_backend.domain.enums.Amenities;
import cm.yowyob.bus_station_backend.domain.enums.StatutVoyage;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class VoyageCreateRequestDTO {
    @NotNull(message = "Le titre du voyage est obligatoire")
    private String titre;
    @NotNull(message = "La description du voyage est obligatoire")
    private String description;

    @NotNull(message = "La date de départ prévue est obligatoire")
    private LocalDateTime dateDepartPrev;

    @NotNull(message = "Le lieu de départ est obligatoire")
    private String lieuDepart;

    @NotNull(message = "Le lieu d'arrivée est obligatoire")
    private String lieuArrive;

    @NotNull(message = "L'heure d'arrivée est obligatoire")
    private LocalDateTime heureArrive;

    @NotNull(message = "Le point de départ est obligatoire")
    private String pointDeDepart;

    @NotNull(message = "Le point d'arrivée est obligatoire")
    private String pointArrivee;

    @NotNull(message = "Le nombre de places réservables est obligatoire")
    private int nbrPlaceReservable; // Nbre de place qu'on peut encore reserver

    private LocalDateTime heureDepartEffectif; // Heure de départ effective

    private int nbrPlaceReserve; // Nbre de place qu'on a reserve
    private int nbrPlaceConfirm; // Nbre de place qu'on a confirmer
    private StatutVoyage statusVoyage;

    @NotNull(message = "Le nombre de places restantes est obligatoire")
    private int nbrPlaceRestante;

    @NotNull(message = "La date limite de réservation est obligatoire")
    private LocalDateTime dateLimiteReservation;

    @NotNull(message = "La date limite de confirmation est obligatoire")
    private LocalDateTime dateLimiteConfirmation;

    private String smallImage;
    private String bigImage;

    // Chauffeur id
    @NotNull(message = "L'identifiant du chauffeur est obligatoire")
    private UUID chauffeurId;

    // Vehicule id
    @NotNull(message = "L'identifiant du véhicule est obligatoire")
    private UUID vehiculeId;

    // Classe voyage id
    @NotNull(message = "L'identifiant de la classe de voyage est obligatoire")
    private UUID classVoyageId;

    // Agence voyage id
    @NotNull(message = "L'identifiant de l'agence de voyage est obligatoire")
    private UUID agenceVoyageId;

    private List<Amenities> amenities;
}
