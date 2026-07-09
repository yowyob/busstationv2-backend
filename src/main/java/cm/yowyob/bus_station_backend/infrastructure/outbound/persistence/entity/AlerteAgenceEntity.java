package cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.entity;

import cm.yowyob.bus_station_backend.domain.enums.TypeAlerte;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("alertes_agence")
public class AlerteAgenceEntity implements Persistable<UUID> {

    @Id
    @Column("id_alerte")
    private UUID idAlerte;

    @Column("gare_id")
    private UUID gareId;

    @Column("agence_id")
    private UUID agenceId;

    @Column("bsm_id")
    private UUID bsmId;

    @Column("type")
    private String type;

    @Column("message")
    private String message;

    @Column("is_lu")
    private boolean isLu;

    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("lu_at")
    private LocalDateTime luAt;

    // ---- Persistable boilerplate (pattern projet) ----
    @Transient
    private boolean isNew = false;

    public void setAsNew() {
        this.isNew = true;
    }

    @Override
    public UUID getId() {
        return this.idAlerte;
    }

    @Override
    public boolean isNew() {
        return this.isNew;
    }
}