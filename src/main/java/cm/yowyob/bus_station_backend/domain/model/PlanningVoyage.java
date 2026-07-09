package cm.yowyob.bus_station_backend.domain.model;

import cm.yowyob.bus_station_backend.domain.enums.planning.RecurrenceType;
import cm.yowyob.bus_station_backend.domain.enums.planning.StatutPlanning;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Represents a travel planning/schedule for an agency.
 * A planning defines a recurring pattern of voyages (daily, weekly, monthly, yearly).
 * It can be used as a template to quickly create actual Voyage instances.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanningVoyage {
    private UUID idPlanning;
    private UUID idAgenceVoyage;
    private String nom;              // e.g. "Planning Douala-Yaoundé Semaine"
    private String description;
    private RecurrenceType recurrence;
    private StatutPlanning statut;

    private LocalDate dateDebut;     // Start date of validity
    private LocalDate dateFin;       // End date of validity (nullable for indefinite)

    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;

    // Transient - loaded separately
    private List<CreneauPlanning> creneaux;

    /**
     * Checks if the planning is currently valid/active based on dates.
     */
    public boolean estValide(LocalDate today) {
        if (this.statut != StatutPlanning.ACTIF) return false;
        if (today.isBefore(this.dateDebut)) return false;
        return this.dateFin == null || !today.isAfter(this.dateFin);
    }

    public void activer() {
        this.statut = StatutPlanning.ACTIF;
        this.dateModification = LocalDateTime.now();
    }

    public void desactiver() {
        this.statut = StatutPlanning.INACTIF;
        this.dateModification = LocalDateTime.now();
    }

    public void archiver() {
        this.statut = StatutPlanning.ARCHIVE;
        this.dateModification = LocalDateTime.now();
    }
}
