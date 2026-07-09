package cm.yowyob.bus_station_backend.application.port.out;

import cm.yowyob.bus_station_backend.domain.model.AgenceVoyage;
import cm.yowyob.bus_station_backend.domain.model.PolitiqueAnnulation;
import cm.yowyob.bus_station_backend.domain.model.Vehicule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface AgencePersistencePort {
    // --- Agence ---
    Mono<AgenceVoyage> save(AgenceVoyage agence);

    Mono<AgenceVoyage> findById(UUID id);

    Mono<AgenceVoyage> findByChefAgenceId(UUID userId);

    Flux<AgenceVoyage> findByOrganisationId(UUID organizationId);

    Mono<Page<AgenceVoyage>> findAll(Pageable pageable);

    Mono<Boolean> existsByLongName(String longName);

    Mono<Boolean> existsByShortName(String shortName);

    Mono<Void> deleteById(UUID id);

    Flux<AgenceVoyage> findByGareRoutiereId(UUID gareRoutiereId);

    // --- Véhicules ---
    Mono<Vehicule> saveVehicule(Vehicule vehicule);

    Mono<Vehicule> findVehiculeById(UUID id);

    Flux<Vehicule> findVehiculesByAgenceId(UUID agenceId);

    Mono<Void> deleteVehiculeById(UUID id);

    // --- Politique Annulation ---
    Mono<PolitiqueAnnulation> savePolitique(PolitiqueAnnulation politique);

    Mono<PolitiqueAnnulation> findPolitiqueByAgenceId(UUID agenceId);

    Mono<PolitiqueAnnulation> findPolitiqueById(UUID id);
}
