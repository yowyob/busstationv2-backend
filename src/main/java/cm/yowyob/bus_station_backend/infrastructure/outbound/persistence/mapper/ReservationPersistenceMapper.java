package cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.mapper;

import cm.yowyob.bus_station_backend.domain.model.Reservation;
import cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.entity.ReservationEntity;
import org.springframework.stereotype.Component;

@Component
public class ReservationPersistenceMapper {

    public Reservation toDomain(ReservationEntity entity) {
        if (entity == null) { return null; }
        return Reservation.builder().idReservation(entity.getIdReservation())
                .idUser(entity.getIdUser())
                .dateReservation(entity.getDateReservation())
                .dateConfirmation(entity.getDateConfirmation())
                .idVoyage(entity.getIdVoyage())
                .montantPaye(entity.getMontantPaye())
                .nbrPassager(entity.getNbrPassager())
                .statutReservation(entity.getStatutReservation())
                .prixTotal(entity.getPrixTotal())
                .statutPayement(entity.getStatutPayement())
                .transactionCode(entity.getTransactionCode())
                .build();
    }

    public ReservationEntity toEntity(Reservation domain) {
        if (domain == null) { return null; }
        return ReservationEntity.builder()
                .idReservation(domain.getIdReservation())
                .idUser(domain.getIdUser())
                .dateReservation(domain.getDateReservation())
                .dateConfirmation(domain.getDateConfirmation())
                .idVoyage(domain.getIdVoyage())
                .statutReservation(domain.getStatutReservation())
                .nbrPassager(domain.getNbrPassager())
                .montantPaye(domain.getMontantPaye())
                .prixTotal(domain.getPrixTotal())
                .statutPayement(domain.getStatutPayement())
                .transactionCode(domain.getTransactionCode())
                .build();
    }
}
