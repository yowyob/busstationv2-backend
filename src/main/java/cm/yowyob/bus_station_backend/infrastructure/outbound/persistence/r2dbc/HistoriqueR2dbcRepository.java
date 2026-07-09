package cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.r2dbc;

import cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.entity.HistoriqueEntity;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface HistoriqueR2dbcRepository extends R2dbcRepository<HistoriqueEntity, UUID> {
    Mono<HistoriqueEntity> findByIdReservation(UUID reservationId);
    // Flux<HistoriqueEntity> findByUserId(UUID userId);
}
