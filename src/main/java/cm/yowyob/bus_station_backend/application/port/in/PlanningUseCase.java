package cm.yowyob.bus_station_backend.application.port.in;

import cm.yowyob.bus_station_backend.application.dto.planning.*;
import cm.yowyob.bus_station_backend.application.dto.voyage.VoyageDetailsDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface PlanningUseCase {

    // --- CRUD Planning ---
    Mono<PlanningVoyageDTO> createPlanning(PlanningVoyageDTO dto, UUID currentUserId);

    Mono<PlanningVoyageDTO> updatePlanning(UUID planningId, PlanningVoyageDTO dto, UUID currentUserId);

    Mono<Void> deletePlanning(UUID planningId, UUID currentUserId);

    Mono<PlanningVoyageDTO> getPlanningById(UUID planningId);

    Flux<PlanningVoyagePreviewDTO> getPlanningsByAgence(UUID agenceId);

    // --- CRUD Creneaux ---
    Mono<CreneauPlanningDTO> addCreneau(UUID planningId, CreneauPlanningDTO dto, UUID currentUserId);

    Mono<CreneauPlanningDTO> updateCreneau(UUID creneauId, CreneauPlanningDTO dto, UUID currentUserId);

    Mono<Void> deleteCreneau(UUID creneauId, UUID currentUserId);

    Flux<CreneauPlanningDTO> getCreneauxByPlanning(UUID planningId);

    // --- Status management ---
    Mono<PlanningVoyageDTO> activerPlanning(UUID planningId, UUID currentUserId);

    Mono<PlanningVoyageDTO> desactiverPlanning(UUID planningId, UUID currentUserId);

    // --- Voyage generation from planning template ---
    Flux<VoyageDetailsDTO> generateVoyagesFromPlanning(GenerateVoyagesFromPlanningDTO dto, UUID currentUserId);

    // --- Public consultation ---
    Flux<PlanningVoyagePreviewDTO> getPlanningsActifsByAgence(UUID agenceId);

    Mono<PlanningVoyageDTO> getPlanningPublicById(UUID planningId);
}
