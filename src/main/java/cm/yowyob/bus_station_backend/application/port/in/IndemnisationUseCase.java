package cm.yowyob.bus_station_backend.application.port.in;

import cm.yowyob.bus_station_backend.domain.model.Coupon;
import cm.yowyob.bus_station_backend.domain.model.SoldeIndemnisation;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface IndemnisationUseCase {
    // Coupons
    Flux<Coupon> getCouponsByUserId(UUID userId, Pageable pageable);
    Flux<Coupon> getCouponsByUserIdAndAgenceId(UUID userId, UUID aganceId, Pageable pageable);
    Mono<Coupon> getCouponById(UUID id);
    // Mono<Coupon> useCoupon(UUID couponId); // Logique métier future ?

    // Solde Agence/User
    Mono<SoldeIndemnisation> getSoldeById(UUID id);
    Flux<SoldeIndemnisation> getAllSoldes();

    // Logique pour appliquer un coupon à une réservation (Déduit du besoin métier)
    Mono<Boolean> applyCouponToReservation(UUID couponId, UUID reservationId, UUID userId);

    // Récupérer le solde d'indemnisation d'un utilisateur auprès d'une agence spécifique
    Mono<SoldeIndemnisation> getSoldeByUserIdAndAgenceId(UUID userId, UUID agenceId);
    Flux<SoldeIndemnisation> getSoldesByUserId(UUID userId, Pageable pageable);
}
