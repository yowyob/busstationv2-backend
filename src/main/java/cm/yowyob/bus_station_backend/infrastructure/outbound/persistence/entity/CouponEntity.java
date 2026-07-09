package cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.entity;

import cm.yowyob.bus_station_backend.domain.enums.StatutCoupon;
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

@Table("coupons")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouponEntity implements Persistable<UUID> {
    @Id
    private UUID idCoupon;
    private LocalDateTime dateDebut;
    private LocalDateTime dateFin;
    private StatutCoupon statusCoupon;
    private double valeur;
    private UUID idHistorique;
    private UUID idSoldeIndemnisation;

    @Transient
    private boolean isNew = false;

    @Override
    public UUID getId() { return idCoupon; }

    @Override
    public boolean isNew() { return isNew || idCoupon == null; }

    public void setAsNew() { this.isNew = true; }
}
