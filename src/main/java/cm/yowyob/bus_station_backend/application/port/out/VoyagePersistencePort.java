package cm.yowyob.bus_station_backend.application.port.out;

import cm.yowyob.bus_station_backend.domain.model.ClassVoyage;
import cm.yowyob.bus_station_backend.domain.model.LigneVoyage;
import cm.yowyob.bus_station_backend.domain.model.Voyage;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.UUID;

public interface VoyagePersistencePort {
    // --- Voyage ---
    Mono<Voyage> save(Voyage voyage);
    Mono<Voyage> findById(UUID id);
    Flux<Voyage> findAll(Pageable pageable);
    Flux<Voyage> findByAgenceId(UUID agenceId, Pageable pageable);
    Flux<Voyage> findByPointName(String pointName, Pageable pageable);
    Mono<Void> deleteById(UUID id);
    Mono<Long> countVoyagesByAgenceId(UUID agenceId);
    Mono<Long> countVoyagesByPointName(String pointName);

    // --- Ligne de Voyage ---
    Mono<LigneVoyage> saveLigneVoyage(LigneVoyage ligneVoyage);
    Mono<LigneVoyage> findLigneVoyageByVoyageId(UUID voyageId);
    Flux<LigneVoyage> findLignesVoyageByAgenceId(UUID agenceId);

    // --- Classe de Voyage ---
    Mono<ClassVoyage> saveClassVoyage(ClassVoyage classVoyage);
    Mono<ClassVoyage> findClassVoyageById(UUID id);
    Flux<ClassVoyage> findAllClassVoyages(Pageable pageable);
    Flux<ClassVoyage> findClassVoyagesByAgence(UUID agenceId);
    Mono<Void> deleteClassVoyageById(UUID id);

    Flux<Integer> findOccupiedPlacesByVoyageId(UUID voyageId);

    Flux<Voyage> findPublicByAgenceId(UUID agenceId, java.time.LocalDateTime now, Pageable pageable);
    Mono<Long> countPublicByAgenceId(UUID agenceId, java.time.LocalDateTime now);
    Flux<Voyage> findSimilaires(UUID excludeId, String lieuDepart, String lieuArrive, UUID agenceId, java.time.LocalDateTime now, int limit);
    Flux<Voyage> searchVoyages(String lieuDepart, String lieuArrive, String date, UUID classId, UUID agenceId, Pageable pageable);

    Mono<Long> countSearchVoyages(String lieuDepart, String lieuArrive, String date, UUID classId, UUID agenceId);
    // --- LOT 5 : Disponibilité ressources ---
    Flux<UUID> findVoyagesUsingVehiculeBetween(UUID vehiculeId, java.time.LocalDateTime start, java.time.LocalDateTime end);
    Flux<UUID> findVoyagesUsingChauffeurBetween(UUID chauffeurId, java.time.LocalDateTime start, java.time.LocalDateTime end);

    // --- LOT 8 : Voyages par gare ---
    /**
     * Voyages d'une gare à une date précise (date_depart_prev::date = date).
     * Si date == null, retourne tous les voyages de la gare paginés.
     */
    Flux<Voyage> findByGareRoutiereIdAndDate(UUID gareId, LocalDate date, Pageable pageable);

    Mono<Long> countByGareRoutiereIdAndDate(UUID gareId, LocalDate date);

    // --- LOT 8 : Statistiques BSM ---
    Mono<Long> countVoyagesByGareIdAndDate(UUID gareId, LocalDate date);
    Mono<Long> countPublicVoyagesByGareIdAfter(UUID gareId, java.time.LocalDateTime now);
    Mono<Double> avgTauxRemplissageByGareId(UUID gareId);
}
