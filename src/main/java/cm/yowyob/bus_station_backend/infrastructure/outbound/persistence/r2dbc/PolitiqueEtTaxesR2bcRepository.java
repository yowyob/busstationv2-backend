package cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.r2dbc;

import java.util.UUID;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;

import cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.entity.PolitiqueEtTaxesEntity;
import reactor.core.publisher.Flux;

public interface PolitiqueEtTaxesR2bcRepository extends R2dbcRepository<PolitiqueEtTaxesEntity, UUID> {

    Flux<PolitiqueEtTaxesEntity> findByGareRoutiereId(UUID gareRoutiereId);

    /**
     * LOT 9 : filtre par type (TAXE / POLITIQUE).
     * Cast explicite varchar pour éviter les soucis de mapping enum côté R2DBC PG.
     */
    @Query("""
        SELECT * FROM politique_et_taxes
        WHERE gare_routiere_id = :gareRoutiereId
          AND type = :type
        ORDER BY date_effet DESC NULLS LAST
        """)
    Flux<PolitiqueEtTaxesEntity> findByGareRoutiereIdAndType(UUID gareRoutiereId, String type);
}
