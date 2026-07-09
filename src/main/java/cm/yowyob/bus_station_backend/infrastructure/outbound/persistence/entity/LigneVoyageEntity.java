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

@Table("lignes_voyage")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LigneVoyageEntity implements Persistable<UUID> {
    @Id
    private UUID idLigneVoyage;
    private UUID idClassVoyage;
    private UUID idVehicule;
    private UUID idVoyage;
    private UUID idAgenceVoyage;
    private UUID idChauffeur;

    @Transient
    private boolean isNew = false;

    @Override
    public UUID getId() { return idLigneVoyage; }

    @Override
    public boolean isNew() { return isNew || idLigneVoyage == null; }

    public void setAsNew() { this.isNew = true; }
}
