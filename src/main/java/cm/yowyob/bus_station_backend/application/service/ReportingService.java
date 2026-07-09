package cm.yowyob.bus_station_backend.application.service;

import cm.yowyob.bus_station_backend.application.dto.statistic.AgenceEvolutionDTO;
import cm.yowyob.bus_station_backend.application.dto.statistic.AgenceStatisticsDTO;
import cm.yowyob.bus_station_backend.application.dto.statistic.EvolutionData;
import cm.yowyob.bus_station_backend.application.port.in.ReportingUseCase;
import cm.yowyob.bus_station_backend.application.port.out.AgencePersistencePort;
import cm.yowyob.bus_station_backend.application.port.out.ReservationPersistencePort;
import cm.yowyob.bus_station_backend.application.port.out.VoyagePersistencePort;
import cm.yowyob.bus_station_backend.domain.enums.StatutHistorique;
import cm.yowyob.bus_station_backend.domain.enums.StatutReservation;
import cm.yowyob.bus_station_backend.domain.enums.StatutVoyage;
import cm.yowyob.bus_station_backend.domain.exception.ResourceNotFoundException;
import cm.yowyob.bus_station_backend.domain.model.Historique;
import cm.yowyob.bus_station_backend.domain.model.Reservation;
import cm.yowyob.bus_station_backend.domain.model.Voyage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportingService implements ReportingUseCase {

    private final ReservationPersistencePort reservationPort;
    private final VoyagePersistencePort voyagePort;
    private final AgencePersistencePort agencePort;

    @Override
    public Mono<AgenceStatisticsDTO> getAgenceStatistics(UUID agenceId) {
        log.info("Génération des statistiques pour l'agence : {}", agenceId);

        return agencePort.findById(agenceId)
                .flatMap(agence -> Mono.zip(
                        // 1. Personnel (Employés + Chauffeurs)
                        agencePort.findVehiculesByAgenceId(agenceId).count(), // Simulation pour l'exemple
                        agencePort.findVehiculesByAgenceId(agenceId).count(), // À remplacer par les vrais ports personnels

                        // 2. Voyages & Statuts
                        voyagePort.countVoyagesByAgenceId(agenceId),
                        getVoyagesStatsParStatut(agenceId),

                        // 3. Réservations & Revenus
                        reservationPort.countReservationsByAgenceId(agenceId),
                        getReservationsStatsParStatut(agenceId),
                        reservationPort.sumRevenusByAgenceId(agenceId).defaultIfEmpty(0.0),

                        // 4. Occupation
                        calculateTauxOccupation(agenceId)
                ).map(tuple -> {
                    AgenceStatisticsDTO stats = new AgenceStatisticsDTO();
                    stats.setNombreEmployes(tuple.getT1()); // Note: À affiner avec un port Employe
                    stats.setNombreChauffeurs(tuple.getT2());
                    stats.setNombreVoyages(tuple.getT3());
                    stats.setVoyagesParStatut(tuple.getT4());
                    stats.setNombreReservations(tuple.getT5());
                    stats.setReservationsParStatut(tuple.getT6());
                    stats.setRevenus(tuple.getT7());
                    stats.setNouveauxUtilisateurs((long) (tuple.getT5() * 0.2)); // Logique de l'ancien backend
                    stats.setTauxOccupation(tuple.getT8());
                    return stats;
                }));
    }

    @Override
    public Mono<AgenceEvolutionDTO> getAgenceEvolution(UUID agenceId) {
        // Génération des 6 derniers mois
        List<LocalDate> months = new ArrayList<>();
        LocalDate now = LocalDate.now();
        for (int i = 5; i >= 0; i--) {
            months.add(now.minusMonths(i).withDayOfMonth(1));
        }

        return voyagePort.findLignesVoyageByAgenceId(agenceId)
                .flatMap(ligne -> reservationPort.findByVoyageId(ligne.getIdVoyage()))
                .collectList()
                .zipWith(voyagePort.findLignesVoyageByAgenceId(agenceId)
                        .flatMap(l -> voyagePort.findById(l.getIdVoyage())).collectList())
                .map(tuple -> {
                    List<Reservation> reservations = tuple.getT1();
                    List<Voyage> voyages = tuple.getT2();

                    AgenceEvolutionDTO evolution = new AgenceEvolutionDTO();
                    evolution.setEvolutionReservations(calculateReservationEvolution(months, reservations));
                    evolution.setEvolutionRevenus(calculateRevenusEvolution(months, reservations));
                    evolution.setEvolutionVoyages(calculateVoyageEvolution(months, voyages));
                    evolution.setEvolutionUtilisateurs(calculateUserEvolution(months, reservations));
                    return evolution;
                });
    }

    @Override
    public Flux<Historique> getUserHistory(UUID userId) {
        return reservationPort.findHistoriqueByUserId(userId)
                .filter(h -> h.getStatusHistorique() == StatutHistorique.VALIDER);
    }

    @Override
    public Mono<Historique> getHistoryDetails(UUID id) {
        return reservationPort.findHistoriqueByReservationId(id)
                .switchIfEmpty(Mono.error(
                        new ResourceNotFoundException("Historique introuvable")
                ));
    }

    @Override
    public Flux<Historique> getHistoryByAgence(UUID agenceId) {
        // Jointure Agence -> Voyage -> Reservation -> Historique
        return voyagePort.findLignesVoyageByAgenceId(agenceId)
                .flatMap(ligne -> reservationPort.findByVoyageId(ligne.getIdVoyage()))
                .flatMap(res -> reservationPort.findHistoriqueByReservationId(res.getIdReservation()));
    }

    // --- Helper Methods (Logique métier de l'ancien backend adaptée au réactif) ---

    private Mono<Map<String, Long>> getVoyagesStatsParStatut(UUID agenceId) {
        return voyagePort.findLignesVoyageByAgenceId(agenceId)
                .flatMap(ligne -> voyagePort.findById(ligne.getIdVoyage()))
                .filter(v -> v.getStatusVoyage() != null)
                .collect(Collectors.groupingBy(v -> v.getStatusVoyage().name(), Collectors.counting()))
                .map(map -> {
                    // S'assurer que tous les statuts sont présents
                    for (StatutVoyage s : StatutVoyage.values()) map.putIfAbsent(s.name(), 0L);
                    return map;
                });
    }

    private Mono<Map<String, Long>> getReservationsStatsParStatut(UUID agenceId) {
        return voyagePort.findLignesVoyageByAgenceId(agenceId)
                .flatMap(ligne -> reservationPort.findByVoyageId(ligne.getIdVoyage()))
                .collect(Collectors.groupingBy(r -> r.getStatutReservation().name(), Collectors.counting()))
                .map(map -> {
                    for (StatutReservation s : StatutReservation.values()) map.putIfAbsent(s.name(), 0L);
                    return map;
                });
    }

    private Mono<Double> calculateTauxOccupation(UUID agenceId) {
        return voyagePort.findLignesVoyageByAgenceId(agenceId)
                .flatMap(ligne -> voyagePort.findById(ligne.getIdVoyage()))
                .reduce(new double[]{0, 0}, (acc, v) -> {
                    acc[0] += (v.getNbrPlaceReservable() + v.getNbrPlaceReserve()); // Total places
                    acc[1] += v.getNbrPlaceReserve(); // Places prises
                    return acc;
                })
                .map(acc -> acc[0] > 0 ? (acc[1] / acc[0]) * 100 : 0.0);
    }

    private List<EvolutionData> calculateReservationEvolution(List<LocalDate> months, List<Reservation> data) {
        return months.stream().map(month -> {
            long count = data.stream().filter(r -> isSameMonth(r.getDateReservation(), month)).count();
            return new EvolutionData(month, count, 0.0);
        }).collect(Collectors.toList());
    }

    private List<EvolutionData> calculateRevenusEvolution(List<LocalDate> months, List<Reservation> data) {
        return months.stream().map(month -> {
            double total = data.stream()
                    .filter(r -> (r.getStatutReservation() == StatutReservation.CONFIRMER || r.getStatutReservation() == StatutReservation.VALIDER))
                    .filter(r -> r.getDateConfirmation() != null && isSameMonth(r.getDateConfirmation(), month))
                    .mapToDouble(Reservation::getMontantPaye)
                    .sum();
            return new EvolutionData(month, 0, total);
        }).collect(Collectors.toList());
    }

    private List<EvolutionData> calculateVoyageEvolution(List<LocalDate> months, List<Voyage> data) {
        return months.stream().map(month -> {
            long count = data.stream()
                    .filter(v -> v.getDatePublication() != null && isSameMonth(v.getDatePublication(), month))
                    .count();
            return new EvolutionData(month, count, 0.0);
        }).collect(Collectors.toList());
    }

    private List<EvolutionData> calculateUserEvolution(List<LocalDate> months, List<Reservation> data) {
        return months.stream().map(month -> {
            long uniqueUsers = data.stream()
                    .filter(r -> isSameMonth(r.getDateReservation(), month))
                    .map(Reservation::getIdUser)
                    .distinct()
                    .count();
            return new EvolutionData(month, uniqueUsers, 0.0);
        }).collect(Collectors.toList());
    }

    private boolean isSameMonth(Object dateObj, LocalDate targetMonth) {
        if (dateObj == null) return false;
        LocalDate date;
        if (dateObj instanceof java.time.LocalDateTime) {
            date = ((java.time.LocalDateTime) dateObj).toLocalDate();
        } else if (dateObj instanceof java.util.Date) {
            date = ((java.util.Date) dateObj).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        } else {
            return false;
        }
        return date.getYear() == targetMonth.getYear() && date.getMonth() == targetMonth.getMonth();
    }
}