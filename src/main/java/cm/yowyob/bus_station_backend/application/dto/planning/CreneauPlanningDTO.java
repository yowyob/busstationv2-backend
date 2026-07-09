package cm.yowyob.bus_station_backend.application.dto.planning;

import cm.yowyob.bus_station_backend.domain.enums.Amenities;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreneauPlanningDTO {

    @JsonProperty("id_creneau")
    private UUID idCreneau;

    @JsonProperty("id_planning")
    private UUID idPlanning;

    // Scheduling
    @JsonProperty("jour_semaine")
    private DayOfWeek jourSemaine;

    @JsonProperty("jour_mois")
    private Integer jourMois;

    private Integer mois;

    // Voyage template
    private String titre;
    private String description;

    @NotNull(message = "L'heure de départ est obligatoire")
    @JsonProperty("heure_depart")
    private LocalTime heureDepart;

    @JsonProperty("heure_arrivee")
    private LocalTime heureArrivee;

    @JsonProperty("duree_estimee")
    private Duration dureeEstimee;

    @NotBlank(message = "Le lieu de départ est obligatoire")
    @JsonProperty("lieu_depart")
    private String lieuDepart;

    @NotBlank(message = "Le lieu d'arrivée est obligatoire")
    @JsonProperty("lieu_arrive")
    private String lieuArrive;

    @JsonProperty("point_de_depart")
    private String pointDeDepart;

    @JsonProperty("point_arrivee")
    private String pointArrivee;

    // Resources
    @NotNull(message = "L'ID de la classe de voyage est obligatoire")
    @JsonProperty("id_class_voyage")
    private UUID idClassVoyage;

    @NotNull(message = "L'ID du véhicule est obligatoire")
    @JsonProperty("id_vehicule")
    private UUID idVehicule;

    @JsonProperty("id_chauffeur")
    private UUID idChauffeur;

    // Reservation settings
    @JsonProperty("nbr_places_disponibles")
    private int nbrPlacesDisponibles;

    @JsonProperty("delai_reservation_heures")
    private int delaiReservationHeures;

    @JsonProperty("delai_confirmation_heures")
    private int delaiConfirmationHeures;

    // Media
    @JsonProperty("small_image")
    private String smallImage;

    @JsonProperty("big_image")
    private String bigImage;

    private List<Amenities> amenities;

    private boolean actif;
}
