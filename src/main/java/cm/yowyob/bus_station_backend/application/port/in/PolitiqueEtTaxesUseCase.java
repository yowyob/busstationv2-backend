package cm.yowyob.bus_station_backend.application.port.in;

import cm.yowyob.bus_station_backend.domain.model.PolitiqueEtTaxes;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface PolitiqueEtTaxesUseCase {

    Mono<PolitiqueEtTaxes> createPolitique(PolitiqueEtTaxes politiqueEtTaxes, Mono<FilePart> filePartMono);

    Mono<PolitiqueEtTaxes> updatePolitique(UUID politiqueId, PolitiqueEtTaxes politiqueEtTaxes, Mono<FilePart> filePartMono);

    Mono<Void> deletePolitique(UUID politiqueId);

    Mono<PolitiqueEtTaxes> getById(UUID politiqueId);

    Flux<PolitiqueEtTaxes> getAllByGareRoutiere(UUID gareRoutiereId);
}
