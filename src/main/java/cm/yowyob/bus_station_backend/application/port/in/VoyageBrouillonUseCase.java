package cm.yowyob.bus_station_backend.application.port.in;

import cm.yowyob.bus_station_backend.application.dto.voyage.VoyageBrouillonCreateDTO;
import cm.yowyob.bus_station_backend.application.dto.voyage.VoyageBrouillonResponseDTO;
import cm.yowyob.bus_station_backend.application.dto.voyage.VoyageBrouillonUpdateDTO;
import cm.yowyob.bus_station_backend.application.dto.voyage.VoyageDetailsDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface VoyageBrouillonUseCase {
    Mono<VoyageBrouillonResponseDTO> create(VoyageBrouillonCreateDTO dto);

    Mono<VoyageBrouillonResponseDTO> getById(UUID id);

    Mono<VoyageBrouillonResponseDTO> update(UUID id, VoyageBrouillonUpdateDTO dto);

    Mono<Void> delete(UUID id);

    Mono<VoyageDetailsDTO> publish(UUID id, UUID currentUserId);

    Flux<VoyageBrouillonResponseDTO> listByAgence(UUID agenceId, String statut);
}