package cm.yowyob.bus_station_backend.application.port.out;

import java.util.UUID;

import cm.yowyob.bus_station_backend.domain.enums.PolitiqueOuTaxe;
import cm.yowyob.bus_station_backend.domain.model.PolitiqueEtTaxes;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PolitiqueEtTaxesPort {

    Mono<PolitiqueEtTaxes> save(PolitiqueEtTaxes politiqueEtTaxes);

    Mono<PolitiqueEtTaxes> findById(UUID politiqueId);

    Flux<PolitiqueEtTaxes> findByGareRoutiereId(UUID gareRoutiereId);

    Mono<Void> deleteById(UUID politiqueId);

    // --- LOT 9 : extensions ---

    /**
     * Retourne uniquement les entrées de la gare ayant le type donné (TAXE ou POLITIQUE).
     */
    Flux<PolitiqueEtTaxes> findByGareRoutiereIdAndType(UUID gareRoutiereId, PolitiqueOuTaxe type);
}
