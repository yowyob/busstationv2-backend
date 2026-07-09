package cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.mapper;

import cm.yowyob.bus_station_backend.domain.model.Coordonnee;
import cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.entity.CoordonneeEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CoordonneePersistenceMapper {

    Coordonnee toDomain(CoordonneeEntity entity);

    CoordonneeEntity toEntity(Coordonnee domain);
}
