package cm.yowyob.bus_station_backend.application.port.in;

import cm.yowyob.bus_station_backend.application.dto.classVoyage.ClassVoyageDTO;
import cm.yowyob.bus_station_backend.application.dto.classVoyage.ClassVoyageResponseDTO;
import cm.yowyob.bus_station_backend.application.dto.voyage.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface VoyageUseCase {
    // --- CRUD Voyage ---
    Mono<VoyageDetailsDTO> createVoyage(VoyageCreateRequestDTO dto, UUID currentUserId);

    Mono<VoyageDetailsDTO> updateVoyage(UUID voyageId, VoyageDTO dto, UUID currentUserId);

    Mono<Void> deleteVoyage(UUID voyageId, UUID currentUserId);

    Mono<VoyageDetailsDTO> getVoyageById(UUID voyageId);

    Mono<Page<VoyagePreviewDTO>> getAllVoyagesPreview(Pageable pageable);

    Mono<Page<VoyagePreviewDTO>> getAllVoyages(Pageable pageable);

    Mono<Page<VoyagePreviewDTO>> getVoyagesByAgence(UUID agenceId, Pageable pageable);
    Mono<Page<VoyagePreviewDTO>> getVoyagesByGareRoutiere(UUID gareId, Pageable pageable);
    Mono<VoyageDetailsDTO> updateVoyageStatus(UUID voyageId, String newStatus, UUID currentUserId);

    Mono<VoyageDetailsDTO> assignChauffeurAndVehicule(UUID voyageId, UUID chauffeurId, UUID vehiculeId,
            UUID currentUserId);

    Flux<Integer> getOccupiedPlaces(UUID voyageId);

    // Méthode de recherche avancée (si applicable)
    // Mono<Page<VoyagePreviewDTO>> searchVoyages(VoyageSearchCriteria criteria,
    // Pageable pageable);

    // class voyage
    Mono<ClassVoyageDTO> createClassVoyage(ClassVoyageDTO dto);

    Mono<ClassVoyageDTO> updateClassVoyage(UUID classVoyageId, ClassVoyageDTO dto);

    Mono<Void> deleteClassVoyage(UUID classVoyageId);

    Mono<ClassVoyageDTO> getClassVoyageById(UUID classVoyageId);

    Mono<Page<ClassVoyageDTO>> getAllClassVoyages(Pageable pageable);

    Flux<ClassVoyageResponseDTO> getClassVoyagesByAgence(UUID agenceId);

    Flux<VoyagePreviewDTO> getVoyagesSimilaires(UUID voyageId, int limit);
    Mono<Page<VoyagePreviewDTO>> getVoyagesPublicsByAgence(UUID agenceId, Pageable pageable);
    Mono<Page<VoyagePreviewDTO>> searchVoyages(String lieuDepart, String lieuArrive, String date, UUID classId, UUID agenceId, Pageable pageable);

}
