package cm.yowyob.bus_station_backend.application.port.in;

import cm.yowyob.bus_station_backend.application.dto.disponibilite.DisponibiliteResponseDTO;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface DisponibiliteUseCase {
    /**
     * Vérifie la disponibilité d'un véhicule à une date (et optionnellement une heure précise).
     * @param vehiculeId  identifiant du véhicule
     * @param date        date au format YYYY-MM-DD
     * @param heure       heure au format HH:mm, ou null pour vérifier toute la journée
     */
    Mono<DisponibiliteResponseDTO> checkVehiculeDisponibilite(UUID vehiculeId, String date, String heure);

    /**
     * Idem pour un chauffeur.
     */
    Mono<DisponibiliteResponseDTO> checkChauffeurDisponibilite(UUID chauffeurId, String date, String heure);
}