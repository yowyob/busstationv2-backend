package cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.mapper;

import cm.yowyob.bus_station_backend.domain.model.Historique;
import cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.entity.HistoriqueEntity;
import org.springframework.stereotype.Component;

@Component
public class HistoriquePersistenceMapper {

    public Historique toDomain(HistoriqueEntity entity) {
        if (entity == null) return null;

        return Historique.builder()
                .idHistorique(entity.getIdHistorique())
                .statusHistorique(entity.getStatusHistorique())
                .dateReservation(entity.getDateReservation())
                .dateConfirmation(entity.getDateConfirmation())
                .dateAnnulation(entity.getDateAnnulation())
                .causeAnnulation(entity.getCauseAnnulation())
                .origineAnnulation(entity.getOrigineAnnulation())
                .tauxAnnulation(entity.getTauxAnnulation())
                .compensation(entity.getCompensation())
                .idReservation(entity.getIdReservation())
                .build();
    }

    public HistoriqueEntity toEntity(Historique domain) {
        if (domain == null) return null;

        return HistoriqueEntity.builder()
                .idHistorique(domain.getIdHistorique())
                .statusHistorique(domain.getStatusHistorique())
                .dateReservation(domain.getDateReservation())
                .dateConfirmation(domain.getDateConfirmation())
                .dateAnnulation(domain.getDateAnnulation())
                .causeAnnulation(domain.getCauseAnnulation())
                .origineAnnulation(domain.getOrigineAnnulation())
                .tauxAnnulation(domain.getTauxAnnulation())
                .compensation(domain.getCompensation())
                .idReservation(domain.getIdReservation())
                .build();
    }
}

