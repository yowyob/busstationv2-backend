package cm.yowyob.bus_station_backend.domain.model;

import cm.yowyob.bus_station_backend.domain.enums.Amenities;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

/**
 * Represents a scheduled time slot within a planning.
 * Each creneau defines a voyage template: departure/arrival info, time,
 * vehicle, driver, class, etc.
 *
 * For QUOTIDIEN (daily) planning: jourSemaine can be null (applies every day).
 * For HEBDOMADAIRE (weekly) planning: jourSemaine specifies the day of the week.
 * For MENSUEL (monthly) planning: jourMois specifies the day of the month (1-31).
 * For ANNUEL (yearly) planning: jourMois + mois specify the date in the year.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreneauPlanning {
    private UUID idCreneau;
    private UUID idPlanning;

    // Scheduling fields
    private DayOfWeek jourSemaine;   // For weekly recurrence (MONDAY, TUESDAY, etc.)
    private Integer jourMois;         // For monthly recurrence (1-31)
    private Integer mois;             // For yearly recurrence (1-12)

    // Voyage template fields
    private String titre;
    private String description;
    private LocalTime heureDepart;
    private LocalTime heureArrivee;
    private Duration dureeEstimee;
    private String lieuDepart;
    private String lieuArrive;
    private String pointDeDepart;
    private String pointArrivee;

    // Resources
    private UUID idClassVoyage;
    private UUID idVehicule;
    private UUID idChauffeur;

    // Reservation settings
    private int nbrPlacesDisponibles;
    private int delaiReservationHeures;   // Hours before departure to close reservations
    private int delaiConfirmationHeures;  // Hours before departure to close confirmations

    // Images & amenities
    private String smallImage;
    private String bigImage;
    private String amenities; // CSV string of Amenities

    private boolean actif; // Whether this specific slot is active

    public void setAmenitiesList(List<Amenities> amenitiesList) {
        if (amenitiesList == null || amenitiesList.isEmpty()) {
            this.amenities = "";
            return;
        }
        this.amenities = amenitiesList.stream()
                .map(Amenities::name)
                .reduce((a, b) -> a + "," + b)
                .orElse("");
    }

    public List<Amenities> getAmenitiesList() {
        if (amenities == null || amenities.isEmpty()) {
            return List.of();
        }
        return List.of(amenities.split(",")).stream()
                .map(Amenities::valueOf)
                .toList();
    }
}
