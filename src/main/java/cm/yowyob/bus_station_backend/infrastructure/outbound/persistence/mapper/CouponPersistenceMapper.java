package cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.mapper;

import cm.yowyob.bus_station_backend.domain.model.Coupon;
import cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.entity.CouponEntity;
import org.springframework.stereotype.Component;

@Component
public class CouponPersistenceMapper {

    public Coupon toDomain(CouponEntity entity) {
        if (entity == null) return null;

        return Coupon.builder()
                .idCoupon(entity.getIdCoupon())
                .dateDebut(entity.getDateDebut())
                .dateFin(entity.getDateFin())
                .statusCoupon(entity.getStatusCoupon())
                .valeur(entity.getValeur())
                .idHistorique(entity.getIdHistorique())
                .idSoldeIndemnisation(entity.getIdSoldeIndemnisation())
                .build();
    }

    public CouponEntity toEntity(Coupon domain) {
        if (domain == null) return null;

        return CouponEntity.builder()
                .idCoupon(domain.getIdCoupon())
                .dateDebut(domain.getDateDebut())
                .dateFin(domain.getDateFin())
                .statusCoupon(domain.getStatusCoupon())
                .valeur(domain.getValeur())
                .idHistorique(domain.getIdHistorique())
                .idSoldeIndemnisation(domain.getIdSoldeIndemnisation())
                .build();
    }
}

