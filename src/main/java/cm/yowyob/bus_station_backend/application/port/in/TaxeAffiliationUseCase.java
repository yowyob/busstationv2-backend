package cm.yowyob.bus_station_backend.application.port.in;

import java.util.UUID;

import cm.yowyob.bus_station_backend.application.dto.taxe.TaxeAffiliationAgenceResponseDTO;
import cm.yowyob.bus_station_backend.application.dto.taxe.TaxeAffiliationCreateDTO;
import cm.yowyob.bus_station_backend.application.dto.taxe.TaxeAffiliationResponseDTO;
import cm.yowyob.bus_station_backend.application.dto.taxe.TaxeAffiliationUpdateDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TaxeAffiliationUseCase {

    Flux<TaxeAffiliationResponseDTO> getByGareRoutiereId(UUID gareRoutiereId);

    Mono<TaxeAffiliationAgenceResponseDTO> getByAgence(UUID agencyId);

    Mono<TaxeAffiliationResponseDTO> getById(UUID id);

    Mono<TaxeAffiliationResponseDTO> create(TaxeAffiliationCreateDTO dto);

    Mono<TaxeAffiliationResponseDTO> update(UUID id, TaxeAffiliationUpdateDTO dto);

    Mono<Void> delete(UUID id);
}
