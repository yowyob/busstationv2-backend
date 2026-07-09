package cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.entity;

import cm.yowyob.bus_station_backend.domain.enums.planning.RecurrenceType;
import cm.yowyob.bus_station_backend.domain.enums.planning.StatutPlanning;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Table("plannings_voyage")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanningVoyageEntity implements Persistable<UUID> {

    @Id
    @Column("id_planning")
    private UUID idPlanning;

    @Column("id_agence_voyage")
    private UUID idAgenceVoyage;

    @Column("nom")
    private String nom;

    @Column("description")
    private String description;

    @Column("recurrence")
    private RecurrenceType recurrence;

    @Column("statut")
    private StatutPlanning statut;

    @Column("date_debut")
    private LocalDate dateDebut;

    @Column("date_fin")
    private LocalDate dateFin;

    @Column("date_creation")
    private LocalDateTime dateCreation;

    @Column("date_modification")
    private LocalDateTime dateModification;

    @Transient
    private boolean isNew = false;

    @Override
    public UUID getId() {
        return this.idPlanning;
    }

    @Override
    public boolean isNew() {
        return this.isNew || this.idPlanning == null;
    }

    public void setAsNew() {
        this.isNew = true;
    }
}