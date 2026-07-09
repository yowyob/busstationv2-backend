package cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Table("voyages_brouillon")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoyageBrouillonEntity implements Persistable<UUID> {
    @Id
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
    private String amenities;

    private String smallImage;
    private String bigImage;

    private LocalDateTime dateLimiteReservation;
    private LocalDateTime dateLimiteConfirmation;

    private String statutBrouillon;
    private String notes;

    private UUID voyageId;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Transient
    private boolean isNew = false;

    @Override
    public UUID getId() { return id; }

    @Override
    public boolean isNew() { return isNew || id == null; }

    public void setAsNew() { this.isNew = true; }
}