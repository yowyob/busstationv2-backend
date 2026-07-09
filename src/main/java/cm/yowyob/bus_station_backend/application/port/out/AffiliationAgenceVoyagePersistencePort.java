package cm.yowyob.bus_station_backend.application.port.out;

import java.util.UUID;

import cm.yowyob.bus_station_backend.domain.model.AffiliationAgenceVoyage;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AffiliationAgenceVoyagePersistencePort {

  /**
   * INSERT si entity.id == null (générée), UPDATE sinon.
   * Le adapter gère la convention Persistable + setAsNew().
   */
  Mono<AffiliationAgenceVoyage> save(AffiliationAgenceVoyage affiliationAgenceVoyage);

  Flux<AffiliationAgenceVoyage> findByGareRoutiereId(UUID gareRoutiereId);

  // --- LOT 9 : extensions ---
  Mono<AffiliationAgenceVoyage> findById(UUID id);

  Flux<AffiliationAgenceVoyage> findByAgencyId(UUID agencyId);

  Mono<Void> deleteById(UUID id);
}
