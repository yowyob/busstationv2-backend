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

@Table("soldes_indemnisation")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SoldeIndemnisationEntity implements Persistable<UUID> {
    @Id
    private UUID idSolde;
    private double solde;
    private String type;
    private UUID idUser;
    private UUID idAgenceVoyage;

    @Transient
    private boolean isNew = false;

    @Override
    public UUID getId() { return idSolde; }

    @Override
    public boolean isNew() { return isNew || idSolde == null; }

    public void setAsNew() { this.isNew = true; }
}
