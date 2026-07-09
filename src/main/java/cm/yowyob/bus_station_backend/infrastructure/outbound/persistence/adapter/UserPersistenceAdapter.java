package cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.adapter;

import cm.yowyob.bus_station_backend.application.port.out.UserPersistencePort;
import cm.yowyob.bus_station_backend.domain.model.ChauffeurAgenceVoyage;
import cm.yowyob.bus_station_backend.domain.model.EmployeAgenceVoyage;
import cm.yowyob.bus_station_backend.domain.model.User;
import cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.entity.ChauffeurEntity;
import cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.entity.EmployeEntity;
import cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.entity.UserEntity;
import cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.mapper.ChauffeurPersistenceMapper;
import cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.mapper.EmployePersistenceMapper;
import cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.mapper.UserPersistenceMapper;
import cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.r2dbc.ChauffeurR2dbcRepository;
import cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.r2dbc.EmployeR2dbcRepository;
import cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.r2dbc.UserR2dbcRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserPersistenceAdapter implements UserPersistencePort {

    private final UserR2dbcRepository userRepository;
    private final EmployeR2dbcRepository employeRepository;
    private final ChauffeurR2dbcRepository chauffeurRepository;

    private final UserPersistenceMapper userMapper;
    private final EmployePersistenceMapper employeMapper;
    private final ChauffeurPersistenceMapper chauffeurMapper;

    @Override
    public Mono<User> save(User user) {
        UserEntity entity = userMapper.toEntity(user);
        if (user.getUserId() == null) {
            entity.setUserId(UUID.randomUUID());
            entity.setAsNew();
            return userRepository.save(entity).map(userMapper::toDomain);
        }
        return userRepository.existsById(user.getUserId())
                .flatMap(exists -> {
                    if (!exists) {
                        entity.setAsNew();
                    }
                    return userRepository.save(entity);
                })
                .map(userMapper::toDomain);
    }

    @Override
    public Mono<User> findById(UUID id) {
        return userRepository.findById(id)
                .map(userMapper::toDomain);
    }

    @Override
    public Mono<User> findByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(userMapper::toDomain);
    }

    @Override
    public Mono<User> findByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(userMapper::toDomain);
    }

    @Override
    public Mono<Boolean> existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public Mono<Boolean> existsByTelNumber(String telNumber) {
        return userRepository.existsByTelNumber(telNumber);
    }

    @Override
    public Mono<Boolean> existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public Mono<Void> deleteById(UUID id) {
        return userRepository.deleteById(id);
    }

    @Override
    public Mono<EmployeAgenceVoyage> saveEmploye(EmployeAgenceVoyage employe) {
        EmployeEntity entity = employeMapper.toEntity(employe);
        if (employe.getEmployeId() == null) {
            entity.setEmployeId(UUID.randomUUID());
            entity.setAsNew();
            return employeRepository.save(entity).map(employeMapper::toDomain);
        }
        return employeRepository.existsById(employe.getEmployeId())
                .flatMap(exists -> {
                    if (!exists) {
                        entity.setAsNew();
                    }
                    return employeRepository.save(entity);
                })
                .map(employeMapper::toDomain);
    }

    @Override
    public Mono<EmployeAgenceVoyage> findEmployeById(UUID id) {
        return employeRepository.findById(id)
                .map(employeMapper::toDomain);
    }

    @Override
    public Flux<EmployeAgenceVoyage> findEmployesByAgenceId(UUID agenceId) {
        return employeRepository.findByAgenceVoyageId(agenceId)
                .map(employeMapper::toDomain);
    }

    @Override
    public Mono<Void> deleteEmployeById(UUID id) {
        return employeRepository.deleteById(id);
    }

    @Override
    public Mono<ChauffeurAgenceVoyage> saveChauffeur(ChauffeurAgenceVoyage chauffeur) {
        ChauffeurEntity entity = chauffeurMapper.toEntity(chauffeur);
        if (chauffeur.getChauffeurId() == null) {
            entity.setChauffeurId(UUID.randomUUID());
            entity.setAsNew();
            return chauffeurRepository.save(entity).map(chauffeurMapper::toDomain);
        }
        return chauffeurRepository.existsById(chauffeur.getChauffeurId())
                .flatMap(exists -> {
                    if (!exists) {
                        entity.setAsNew();
                    }
                    return chauffeurRepository.save(entity);
                })
                .map(chauffeurMapper::toDomain);
    }

    @Override
    public Mono<ChauffeurAgenceVoyage> findChauffeurById(UUID id) {
        return chauffeurRepository.findById(id)
                .map(chauffeurMapper::toDomain);
    }

    @Override
    public Mono<ChauffeurAgenceVoyage> findChauffeurByUserId(UUID userId) {
        return chauffeurRepository.findByUserId(userId)
                .map(chauffeurMapper::toDomain);
    }

    @Override
    public Flux<ChauffeurAgenceVoyage> findChauffeursByAgenceId(UUID agenceId) {
        return chauffeurRepository.findByAgenceVoyageId(agenceId)
                .map(chauffeurMapper::toDomain);
    }

    @Override
    public Mono<Void> deleteChauffeurById(UUID id) {
        return chauffeurRepository.deleteById(id);
    }
}
