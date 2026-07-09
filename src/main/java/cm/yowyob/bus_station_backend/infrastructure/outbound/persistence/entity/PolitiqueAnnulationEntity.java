package cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Table("politiques_annulation")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PolitiqueAnnulationEntity implements Persistable<UUID> {

    @Id
    private UUID idPolitique;

    /**
     * Durée de validité du coupon (en secondes)
     */
    private Long dureeCouponSeconds;

    private UUID idAgenceVoyage;

    @Transient
    private boolean isNew = false;

    @Override
    public UUID getId() { return idPolitique; }

    @Override
    public boolean isNew() { return isNew || idPolitique == null; }

    public void setAsNew() { this.isNew = true; }
}
