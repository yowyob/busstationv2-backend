package cm.yowyob.bus_station_backend.application.service;

import cm.yowyob.bus_station_backend.application.dto.disponibilite.DisponibiliteResponseDTO;
import cm.yowyob.bus_station_backend.application.port.in.DisponibiliteUseCase;
import cm.yowyob.bus_station_backend.application.port.out.VoyagePersistencePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class DisponibiliteService implements DisponibiliteUseCase {

    private final VoyagePersistencePort voyagePort;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    @Override
    public Mono<DisponibiliteResponseDTO> checkVehiculeDisponibilite(UUID vehiculeId, String date, String heure) {
        return checkAvailability(vehiculeId, "VEHICULE", date, heure,
                voyagePort::findVoyagesUsingVehiculeBetween);
    }

    @Override
    public Mono<DisponibiliteResponseDTO> checkChauffeurDisponibilite(UUID chauffeurId, String date, String heure) {
        return checkAvailability(chauffeurId, "CHAUFFEUR", date, heure,
                voyagePort::findVoyagesUsingChauffeurBetween);
    }

    // --- Helpers ---

    @FunctionalInterface
    private interface RangeQuery {
        Flux<UUID> apply(UUID resourceId, LocalDateTime start, LocalDateTime end);
    }

    private Mono<DisponibiliteResponseDTO> checkAvailability(
            UUID resourceId, String type, String dateStr, String heureStr, RangeQuery query) {

        LocalDate parsedDate;
        try {
            parsedDate = LocalDate.parse(dateStr, DATE_FMT);
        } catch (Exception e) {
            return Mono.error(new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.BAD_REQUEST,
                "Parametre 'date' invalide (attendu YYYY-MM-DD) : " + dateStr));
        }

        LocalDateTime start;
        LocalDateTime end;
        if (heureStr != null && !heureStr.isBlank()) {
            LocalTime parsedTime;
            try {
                parsedTime = LocalTime.parse(heureStr, TIME_FMT);
            } catch (Exception e) {
                return Mono.error(new org.springframework.web.server.ResponseStatusException(
                      org.springframework.http.HttpStatus.BAD_REQUEST,
                      "Parametre 'heure' invalide (attendu HH:mm) : " + heureStr));
            }
            start = parsedDate.atTime(parsedTime);
            end = start;
        } else {
            start = parsedDate.atStartOfDay();
            end = parsedDate.atTime(23, 59, 59);
        }

        return query.apply(resourceId, start, end)
                .collectList()
                .map(conflicts -> buildResponse(resourceId, type, dateStr, heureStr, conflicts));
    }

    private DisponibiliteResponseDTO buildResponse(UUID resourceId, String type, String date, String heure, List<UUID> conflicts) {
        boolean available = conflicts.isEmpty();
        String msg = available
                ? "Disponible" + (heure != null ? " le " + date + " à " + heure : " toute la journée du " + date)
                : "Indisponible : " + conflicts.size() + " voyage(s) publié(s) utilisent cette ressource";
        return DisponibiliteResponseDTO.builder()
                .resourceId(resourceId)
                .resourceType(type)
                .date(date)
                .heure(heure)
                .available(available)
                .conflictingVoyageIds(conflicts)
                .message(msg)
                .build();
    }
}