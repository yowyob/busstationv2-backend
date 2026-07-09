package cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.mapper;


import cm.yowyob.bus_station_backend.domain.model.PolitiqueAnnulation;
import cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.entity.PolitiqueAnnulationEntity;
import cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.entity.TauxPeriodeEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;

@Component
@RequiredArgsConstructor
public class PolitiqueAnnulationPersistenceMapper {

    private final TauxPeriodePersistenceMapper tauxPeriodeMapper;

    public PolitiqueAnnulation toDomain(PolitiqueAnnulationEntity entity) {
        return PolitiqueAnnulation.builder()
                .idPolitique(entity.getIdPolitique())
                .idAgenceVoyage(entity.getIdAgenceVoyage())
                .dureeCoupon(
                        entity.getDureeCouponSeconds() != null
                                ? Duration.ofSeconds(entity.getDureeCouponSeconds())
                                : null
                )
                .listeTauxPeriode(List.of()) // sera enrichi plus tard
                .build();
    }

    /**
     * Enrichissement de l’agrégat
     */
    public PolitiqueAnnulation enrichWithTaux( PolitiqueAnnulation politique, List<TauxPeriodeEntity> tauxEntities) {
        politique.setListeTauxPeriode(
                tauxEntities.stream()
                        .map(tauxPeriodeMapper::toDomain)
                        .toList()
        );
        return politique;
    }

    public PolitiqueAnnulationEntity toEntity(PolitiqueAnnulation domain) {
        return PolitiqueAnnulationEntity.builder()
                .idPolitique(domain.getIdPolitique())
                .idAgenceVoyage(domain.getIdAgenceVoyage())
                .dureeCouponSeconds(
                        domain.getDureeCoupon() != null
                                ? domain.getDureeCoupon().getSeconds()
                                : null
                )
                .build();
    }
}
