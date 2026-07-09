package cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import cm.yowyob.bus_station_backend.domain.enums.StatutTaxe;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("affiliation_agence_voyage")
public class AffiliationAgenceVoyageEntity implements Persistable<UUID> {

    @Id
    private UUID id;

    @Column("gare_routiere_id")
    private UUID gareRoutiereId;

    @Column("agency_id")
    private UUID agencyId;

    @Column("agency_name")
    private String agencyName;

    @Column("statut")
    private StatutTaxe statut;

    @Column("echeance")
    private LocalDate echeance;

    @Column("montant_affiliation")
    private Double montantAffiliation;

    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("updated_at")
    private LocalDateTime updatedAt;

    @Transient
    @Builder.Default
    private boolean isNew = false;

    @Override
    public boolean isNew() {
        return this.isNew || id == null;
    }

    public void setIsNew(boolean isNew) { this.isNew = isNew; }

    public AffiliationAgenceVoyageEntity setAsNew() {
        this.isNew = true;
        return this;
    }
}
