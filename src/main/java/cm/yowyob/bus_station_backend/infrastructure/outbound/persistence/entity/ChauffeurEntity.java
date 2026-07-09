package cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.entity;

import cm.yowyob.bus_station_backend.domain.enums.StatutChauffeur;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Table("chauffeurs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChauffeurEntity implements Persistable<UUID> {
    @Id
    @Column("id")
    private UUID chauffeurId;
    @Column("agence_id")
    private UUID agenceVoyageId;
    @Column("user_id")
    private UUID userId;
    @Column("statut")
    private StatutChauffeur statusChauffeur;

    @Transient
    private boolean isNew = false;

    @Override
    public UUID getId() { return chauffeurId; }

    @Override
    public boolean isNew() { return isNew || chauffeurId == null; }

    public void setAsNew() { this.isNew = true; }
}
