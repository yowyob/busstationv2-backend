package cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.mapper;

import cm.yowyob.bus_station_backend.domain.model.Vehicule;
import cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.entity.VehiculeEntity;
import org.springframework.stereotype.Component;

@Component
public class VehiculePersistenceMapper {

    public Vehicule toDomain(VehiculeEntity vehiculeEntity) {
        if (vehiculeEntity == null) return null;
        return Vehicule.builder()
                .idVehicule(vehiculeEntity.getIdVehicule())
                .nom(vehiculeEntity.getNom())
                .description(vehiculeEntity.getDescription())
                .idAgenceVoyage(vehiculeEntity.getIdAgenceVoyage())
                .modele(vehiculeEntity.getModele())
                .nbrPlaces(vehiculeEntity.getNbrPlaces())
                .PlaqueMatricule(vehiculeEntity.getPlaqueMatricule())
                .lienPhoto(vehiculeEntity.getLienPhoto())
                .build();
    }

    public VehiculeEntity toEntity(Vehicule vehicule) {
        if (vehicule == null) return null;
        return VehiculeEntity.builder()
                .idVehicule(vehicule.getIdVehicule())
                .nom(vehicule.getNom())
                .description(vehicule.getDescription())
                .modele(vehicule.getModele())
                .PlaqueMatricule(vehicule.getPlaqueMatricule())
                .nbrPlaces(vehicule.getNbrPlaces())
                .idAgenceVoyage(vehicule.getIdAgenceVoyage())
                .lienPhoto(vehicule.getLienPhoto())
                .build();
    }
}
