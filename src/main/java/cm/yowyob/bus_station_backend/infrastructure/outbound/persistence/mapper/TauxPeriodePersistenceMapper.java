package cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.mapper;

import cm.yowyob.bus_station_backend.domain.model.TauxPeriode;
import cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.entity.TauxPeriodeEntity;
import org.springframework.stereotype.Component;

@Component
public class TauxPeriodePersistenceMapper {

    public TauxPeriode toDomain(TauxPeriodeEntity entity) {
        if (entity == null) return null;

        return TauxPeriode.builder()
                .idTauxPeriode(entity.getIdTauxPeriode())
                .dateDebut(entity.getDateDebut())
                .dateFin(entity.getDateFin())
                .valeur(entity.getValeur())
                .idPolitiqueAnnulation(entity.getIdPolitiqueAnnulation())
                .build();
    }

    public TauxPeriodeEntity toEntity(TauxPeriode domain) {
        if (domain == null) return null;

        return TauxPeriodeEntity.builder()
                .idTauxPeriode(domain.getIdTauxPeriode())
                .dateDebut(domain.getDateDebut())
                .dateFin(domain.getDateFin())
                .valeur(domain.getValeur())
                .idPolitiqueAnnulation(domain.getIdPolitiqueAnnulation())
                .build();
    }
}

