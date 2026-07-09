package cm.yowyob.bus_station_backend.domain.model;

import cm.yowyob.bus_station_backend.domain.enums.StatutCoupon;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Coupon {
    private UUID idCoupon;
    private LocalDateTime dateDebut;
    private LocalDateTime dateFin;
    private StatutCoupon statusCoupon;
    private double valeur;
    private UUID idHistorique;
    private UUID idSoldeIndemnisation;
}
