package cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Table("vehicules")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehiculeEntity implements Persistable<UUID> {
    @Id
    private UUID idVehicule;
    private String nom;
    private String modele;
    private String description;
    private int nbrPlaces;
    private String PlaqueMatricule;
    private String lienPhoto;
    private UUID idAgenceVoyage;

    @Transient
    private boolean isNew = false;

    @Override
    public UUID getId() { return idVehicule; }

    @Override
    public boolean isNew() { return isNew || idVehicule == null; }

    public void setAsNew() { this.isNew = true; }
}
