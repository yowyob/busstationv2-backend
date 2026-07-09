package cm.yowyob.bus_station_backend.application.port.out;

import cm.yowyob.bus_station_backend.domain.model.ChauffeurAgenceVoyage;
import cm.yowyob.bus_station_backend.domain.model.EmployeAgenceVoyage;
import cm.yowyob.bus_station_backend.domain.model.User;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface UserPersistencePort {
    Mono<User> save(User user);
    Mono<User> findById(UUID id);
    Mono<User> findByUsername(String username);
    Mono<User> findByEmail(String email);
    Mono<Boolean> existsByEmail(String email);
    Mono<Boolean> existsByTelNumber(String telNumber);
    Mono<Boolean> existsByUsername(String username);
    Mono<Void> deleteById(UUID id);

    // --- Employés ---
    Mono<EmployeAgenceVoyage> saveEmploye(EmployeAgenceVoyage employe);
    Mono<EmployeAgenceVoyage> findEmployeById(UUID id);
    Flux<EmployeAgenceVoyage> findEmployesByAgenceId(UUID agenceId);
    Mono<Void> deleteEmployeById(UUID id);

    // --- Chauffeurs ---
    Mono<ChauffeurAgenceVoyage> saveChauffeur(ChauffeurAgenceVoyage chauffeur);
    Mono<ChauffeurAgenceVoyage> findChauffeurById(UUID id);
    Mono<ChauffeurAgenceVoyage> findChauffeurByUserId(UUID userId); // Lien 1-1
    Flux<ChauffeurAgenceVoyage> findChauffeursByAgenceId(UUID agenceId);
    Mono<Void> deleteChauffeurById(UUID id);
}