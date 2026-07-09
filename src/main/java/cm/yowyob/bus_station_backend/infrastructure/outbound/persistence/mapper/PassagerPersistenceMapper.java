package cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.mapper;

import cm.yowyob.bus_station_backend.domain.model.Passager;
import cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.entity.PassagerEntity;
import org.springframework.stereotype.Component;

@Component
public class PassagerPersistenceMapper {

    public Passager toDomain(PassagerEntity entity) {
        if (entity == null) return null;

        return Passager.builder()
                .idPassager(entity.getIdPassager())
                .numeroPieceIdentific(entity.getNumeroPieceIdentific())
                .nom(entity.getNom())
                .genre(entity.getGenre())
                .age(entity.getAge())
                .nbrBaggage(entity.getNbrBaggage())
                .idReservation(entity.getIdReservation())
                .placeChoisis(entity.getPlaceChoisis())
                .build();
    }

    public PassagerEntity toEntity(Passager domain) {
        if (domain == null) return null;

        return PassagerEntity.builder()
                .idPassager(domain.getIdPassager())
                .numeroPieceIdentific(domain.getNumeroPieceIdentific())
                .nom(domain.getNom())
                .genre(domain.getGenre())
                .age(domain.getAge())
                .nbrBaggage(domain.getNbrBaggage())
                .idReservation(domain.getIdReservation())
                .placeChoisis(domain.getPlaceChoisis())
                .build();
    }
}

