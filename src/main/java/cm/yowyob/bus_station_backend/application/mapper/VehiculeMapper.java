package cm.yowyob.bus_station_backend.application.mapper;

import cm.yowyob.bus_station_backend.application.dto.vehicule.VehiculeDTO;
import cm.yowyob.bus_station_backend.domain.model.Vehicule;
import org.springframework.stereotype.Component;

@Component
public class VehiculeMapper {

    public Vehicule toDomain(VehiculeDTO dto) {
        if (dto == null) {
            return null;
        }

        return Vehicule.builder()
                .nom(dto.getNom())
                .modele(dto.getModele())
                .description(dto.getDescription())
                .nbrPlaces(dto.getNbrPlaces())
                .PlaqueMatricule(dto.getPlaqueMatricule())
                .lienPhoto(dto.getLienPhoto())
                .idAgenceVoyage(dto.getIdAgenceVoyage())
                .build();
    }

    public VehiculeDTO toDTO(Vehicule domain) {
        if (domain == null) {
            return null;
        }

        return new VehiculeDTO(
                domain.getIdVehicule(),
                domain.getNom(),
                domain.getModele(),
                domain.getDescription(),
                domain.getNbrPlaces(),
                domain.getPlaqueMatricule(),
                domain.getLienPhoto(),
                domain.getIdAgenceVoyage()
        );
    }
}