package cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Table("taux_periode")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TauxPeriodeEntity implements Persistable<UUID> {
    @Id
    private UUID idTauxPeriode;
    private double valeur;
    private LocalDateTime dateDebut;
    private LocalDateTime dateFin;
    private UUID idPolitiqueAnnulation;

    @Transient
    private boolean isNew = false;

    @Override
    public UUID getId() { return idTauxPeriode; }

    @Override
    public boolean isNew() { return isNew || idTauxPeriode == null; }

    public void setAsNew() { this.isNew = true; }
}
