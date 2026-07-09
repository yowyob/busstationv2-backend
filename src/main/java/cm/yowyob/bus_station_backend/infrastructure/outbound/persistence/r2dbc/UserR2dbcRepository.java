package cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.r2dbc;

import cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.entity.UserEntity;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface UserR2dbcRepository extends R2dbcRepository<UserEntity, UUID> {

    Mono<UserEntity> findByUsername(String username);

    Mono<UserEntity> findByEmail(String email);

    // Les méthodes 'exists' retournent un Mono<Boolean>
    Mono<Boolean> existsByEmail(String email);

    Mono<Boolean> existsByTelNumber(String telNumber);

    Mono<Boolean> existsByUsername(String username);
}
