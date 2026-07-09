package cm.yowyob.bus_station_backend.application.port.in;

import cm.yowyob.bus_station_backend.application.dto.voyage.generation.*;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface VoyageGenerationUseCase {
    Mono<GenerationResultDTO> genererUnitaire(GenerationUnitaireRequestDTO request, UUID currentUserId);

    Mono<MatchingPreviewResponseDTO> matchingPreview(GenerationSemaineRequestDTO request);

    Mono<GenerationSemaineResponseDTO> genererSemaine(GenerationSemaineRequestDTO request, UUID currentUserId);
}