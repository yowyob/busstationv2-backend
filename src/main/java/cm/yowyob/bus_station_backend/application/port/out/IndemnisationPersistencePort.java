package cm.yowyob.bus_station_backend.application.port.out;

import cm.yowyob.bus_station_backend.domain.model.Coupon;
import cm.yowyob.bus_station_backend.domain.model.SoldeIndemnisation;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface IndemnisationPersistencePort {
    // --- Coupons ---
    Mono<Coupon> saveCoupon(Coupon coupon);
    Mono<Coupon> findCouponById(UUID id);
    Flux<Coupon> findCouponsByUserId(UUID userId, Pageable pageable);
    Flux<Coupon> findCouponsByUserIdAndAgenceId(UUID userId, UUID agenceId, Pageable pageable);
    // Flux<Coupon> findCouponsByHistoriqueId(UUID historiqueId);

    // --- Solde Indemnisation ---
    Mono<SoldeIndemnisation> saveSolde(SoldeIndemnisation solde);
    Mono<SoldeIndemnisation> findSoldeById(UUID id);
    Flux<SoldeIndemnisation> findSoldesByUserId(UUID userId, Pageable pageable);
    Mono<SoldeIndemnisation> findSoldeByUserIdAndAgenceId(UUID userId, UUID agenceId);
}
