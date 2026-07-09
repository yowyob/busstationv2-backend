package cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.mapper;

import cm.yowyob.bus_station_backend.domain.model.Baggage;
import cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.entity.BaggageEntity;
import org.springframework.stereotype.Component;

@Component
public class BaggagePersistenceMapper {

    public Baggage toDomain(BaggageEntity entity) {
        if (entity == null) return null;

        return Baggage.builder()
                .idBaggage(entity.getIdBaggage())
                .nbreBaggage(entity.getNbreBaggage())
                .idPassager(entity.getIdPassager())
                .build();
    }

    public BaggageEntity toEntity(Baggage domain) {
        if (domain == null) return null;

        return BaggageEntity.builder()
                .idBaggage(domain.getIdBaggage())
                .nbreBaggage(domain.getNbreBaggage())
                .idPassager(domain.getIdPassager())
                .build();
    }
}
