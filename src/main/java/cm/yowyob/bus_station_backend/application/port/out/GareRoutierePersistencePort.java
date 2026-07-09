package cm.yowyob.bus_station_backend.application.port.out;

import cm.yowyob.bus_station_backend.domain.enums.ServicesGareRoutiere;
import cm.yowyob.bus_station_backend.domain.model.GareRoutiere;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

public interface GareRoutierePersistencePort {

  Mono<GareRoutiere> saveGareRoutiere(GareRoutiere gareRoutiere);

  Mono<GareRoutiere> getGareRoutiereByManagerId(UUID managerId);

  Mono<Page<GareRoutiere>> findAll(String searchTerm, List<ServicesGareRoutiere> services, Pageable pageable);

  Mono<GareRoutiere> getGareRoutiereById(UUID gareRoutiereId);

  Mono<Long> count();

  // --- LOT 8 ---
    /**
     * UPDATE d'une gare existante (UPSERT-safe : utilise le repo R2DBC).
     */
   Mono<GareRoutiere> updateGareRoutiere(GareRoutiere gareRoutiere);

}
