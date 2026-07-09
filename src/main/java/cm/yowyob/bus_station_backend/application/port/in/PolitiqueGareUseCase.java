package cm.yowyob.bus_station_backend.application.port.in;

import java.util.UUID;

import cm.yowyob.bus_station_backend.application.dto.politiquegare.PolitiqueGareCreateDTO;
import cm.yowyob.bus_station_backend.application.dto.politiquegare.PolitiqueGareResponseDTO;
import cm.yowyob.bus_station_backend.application.dto.politiquegare.PolitiqueGareUpdateDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PolitiqueGareUseCase {

    Flux<PolitiqueGareResponseDTO> getByGareRoutiereId(UUID gareRoutiereId);

    Mono<PolitiqueGareResponseDTO> getById(UUID id);

    Mono<PolitiqueGareResponseDTO> create(PolitiqueGareCreateDTO dto);

    Mono<PolitiqueGareResponseDTO> update(UUID id, PolitiqueGareUpdateDTO dto);

    Mono<Void> delete(UUID id);
}
