package cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalTime;
import java.util.UUID;

@Table("creneaux_planning")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreneauPlanningEntity implements Persistable<UUID> {

    @Id
    @Column("id_creneau")
    private UUID idCreneau;

    @Column("id_planning")
    private UUID idPlanning;

    // Scheduling
    @Column("jour_semaine")
    private String jourSemaine; // Stored as string: MONDAY, TUESDAY, etc.

    @Column("jour_mois")
    private Integer jourMois;

    @Column("mois")
    private Integer mois;

    // Voyage template
    @Column("titre")
    private String titre;

    @Column("description")
    private String description;

    @Column("heure_depart")
    private LocalTime heureDepart;

    @Column("heure_arrivee")
    private LocalTime heureArrivee;

    @Column("duree_estimee_minutes")
    private Long dureeEstimeeMinutes; // Duration stored as minutes

    @Column("lieu_depart")
    private String lieuDepart;

    @Column("lieu_arrive")
    private String lieuArrive;

    @Column("point_de_depart")
    private String pointDeDepart;

    @Column("point_arrivee")
    private String pointArrivee;

    // Resources
    @Column("id_class_voyage")
    private UUID idClassVoyage;

    @Column("id_vehicule")
    private UUID idVehicule;

    @Column("id_chauffeur")
    private UUID idChauffeur;

    // Reservation settings
    @Column("nbr_places_disponibles")
    private int nbrPlacesDisponibles;

    @Column("delai_reservation_heures")
    private int delaiReservationHeures;

    @Column("delai_confirmation_heures")
    private int delaiConfirmationHeures;

    // Media
    @Column("small_image")
    private String smallImage;

    @Column("big_image")
    private String bigImage;

    @Column("amenities")
    private String amenities;

    @Column("actif")
    private boolean actif;

    @Transient
    private boolean isNew = false;

    @Override
    public UUID getId() {
        return this.idCreneau;
    }

    @Override
    public boolean isNew() {
        return this.isNew || this.idCreneau == null;
    }

    public void setAsNew() {
        this.isNew = true;
    }
}