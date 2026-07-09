package cm.yowyob.bus_station_backend.domain.model;

import cm.yowyob.bus_station_backend.domain.enums.Amenities;
import cm.yowyob.bus_station_backend.domain.enums.StatutVoyage;
import cm.yowyob.bus_station_backend.domain.exception.ReservationException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Voyage {
    private UUID idVoyage;
    private UUID idAgenceVoyage;
    private String titre;
    private String description;
    private LocalDateTime dateDepartPrev;
    private String lieuDepart;
    private LocalDateTime dateDepartEffectif;
    private LocalDateTime dateArriveEffectif;
    private String lieuArrive;
    private LocalDateTime heureDepartEffectif;
    private String pointDeDepart;
    private String pointArrivee;
    private Duration dureeVoyage;
    private LocalDateTime heureArrive;
    private int nbrPlaceReservable;// Nbre de place qu'on peut encore reserve
    private int nbrPlaceReserve;// Nbre de place qu'on a reserve
    private int nbrPlaceConfirm;// Nbre de place qu'on a confirmer
    private int nbrPlaceRestante;//
    private LocalDateTime datePublication;
    private LocalDateTime dateLimiteReservation;
    private LocalDateTime dateLimiteConfirmation;
    private StatutVoyage statusVoyage;
    private String smallImage;
    private String bigImage;

    private String amenities; // JSON string for amenities

    public void setAmenities(List<Amenities> amenitiesList) {
        if (amenitiesList == null || amenitiesList.isEmpty()) {
            this.amenities = "";
            return;
        }
        this.amenities = amenitiesList.stream()
                .map(Amenities::name)
                .reduce((a, b) -> a + "," + b)
                .orElse("");
    }

    public List<Amenities> getAmenities() {
        if (amenities == null || amenities.isEmpty()) {
            return List.of();
        }
        return List.of(amenities.split(",")).stream()
                .map(Amenities::valueOf)
                .toList();
    }

    public void reserverPlaces(int nombre, LocalDateTime now) {
        if (this.nbrPlaceReservable < nombre) {
            throw new ReservationException("Pas assez de places disponibles");
        }
        if (now.isAfter(this.dateLimiteReservation)) {
            throw new ReservationException("Date limite de réservation dépassée");
        }
        this.nbrPlaceReservable -= nombre;
        this.nbrPlaceReserve += nombre;
    }

    public void confirmerPlaces(int nombre) {
        // La confirmation déplace du "Réservé" au "Confirmé"
        this.nbrPlaceReserve -= nombre;
        this.nbrPlaceConfirm += nombre;
    }

    public void libererPlaces(int nombre, boolean etaitConfirme) {
        if (etaitConfirme) {
            this.nbrPlaceConfirm = Math.max(0, this.nbrPlaceConfirm - nombre);
        } else {
            this.nbrPlaceReserve = Math.max(0, this.nbrPlaceReserve - nombre);
        }
        this.nbrPlaceReservable += nombre;
        this.nbrPlaceRestante += nombre;
    }

    public void annuler() {
        this.statusVoyage = StatutVoyage.ANNULE;
        // On ne remet pas les places en vente si le voyage est annulé
    }
}
