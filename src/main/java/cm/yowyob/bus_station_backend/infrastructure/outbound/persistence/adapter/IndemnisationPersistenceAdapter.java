package cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.adapter;

import cm.yowyob.bus_station_backend.application.port.out.IndemnisationPersistencePort;
import cm.yowyob.bus_station_backend.domain.model.Coupon;
import cm.yowyob.bus_station_backend.domain.model.SoldeIndemnisation;
import cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.entity.CouponEntity;
import cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.entity.SoldeIndemnisationEntity;
import cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.mapper.CouponPersistenceMapper;
import cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.mapper.SoldeIndemnisationPersistenceMapper;
import cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.r2dbc.CouponR2dbcRepository;
import cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.r2dbc.SoldeIndemnisationR2dbcRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class IndemnisationPersistenceAdapter implements IndemnisationPersistencePort {

    private final CouponR2dbcRepository couponRepository;
    private final SoldeIndemnisationR2dbcRepository soldeRepository;

    private final CouponPersistenceMapper couponMapper;
    private final SoldeIndemnisationPersistenceMapper soldeMapper;

    // ------------------ COUPONS ------------------

    @Override
    public Mono<Coupon> saveCoupon(Coupon coupon) {
        CouponEntity entity = couponMapper.toEntity(coupon);
        if (coupon.getIdCoupon() == null) {
            entity.setIdCoupon(UUID.randomUUID());
            entity.setAsNew();
            return couponRepository.save(entity).map(couponMapper::toDomain);
        }
        return couponRepository.existsById(coupon.getIdCoupon())
                .flatMap(exists -> {
                    if (!exists) {
                        entity.setAsNew();
                    }
                    return couponRepository.save(entity);
                })
                .map(couponMapper::toDomain);
    }

    @Override
    public Mono<Coupon> findCouponById(UUID id) {
        return couponRepository.findById(id)
                .map(couponMapper::toDomain);
    }

    @Override
    public Flux<Coupon> findCouponsByUserId(UUID userId, Pageable pageable) {
        return couponRepository.findByUserIdPaged(userId, pageable)
                .map(couponMapper::toDomain);
    }

    @Override
    public Flux<Coupon> findCouponsByUserIdAndAgenceId(
            UUID userId,
            UUID agenceId,
            Pageable pageable
    ) {
        return couponRepository.findByUserIdAndAgenceIdPaged(userId, agenceId, pageable)
                .map(couponMapper::toDomain);
    }

    // ------------------ SOLDE INDEMNISATION ------------------

    @Override
    public Mono<SoldeIndemnisation> saveSolde(SoldeIndemnisation solde) {
        SoldeIndemnisationEntity entity = soldeMapper.toEntity(solde);
        if (solde.getIdSolde() == null) {
            entity.setIdSolde(UUID.randomUUID());
            entity.setAsNew();
            return soldeRepository.save(entity).map(soldeMapper::toDomain);
        }
        return soldeRepository.existsById(solde.getIdSolde())
                .flatMap(exists -> {
                    if (!exists) {
                        entity.setAsNew();
                    }
                    return soldeRepository.save(entity);
                })
                .map(soldeMapper::toDomain);
    }

    @Override
    public Mono<SoldeIndemnisation> findSoldeById(UUID id) {
        return soldeRepository.findById(id)
                .map(soldeMapper::toDomain);
    }

    @Override
    public Flux<SoldeIndemnisation> findSoldesByUserId(UUID userId, Pageable pageable) {
        return soldeRepository.findByUserIdPaged(userId, pageable)
                .map(soldeMapper::toDomain);
    }

    @Override
    public Mono<SoldeIndemnisation> findSoldeByUserIdAndAgenceId(UUID userId, UUID agenceId) {
        return soldeRepository.findByUserIdAndAgenceId(userId, agenceId)
                .map(soldeMapper::toDomain);
    }
}
