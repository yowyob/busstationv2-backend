package cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.entity;

import cm.yowyob.bus_station_backend.domain.enums.StatutHistorique;
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

@Table("historiques")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HistoriqueEntity implements Persistable<UUID> {
    @Id
    private UUID idHistorique;
    private StatutHistorique statusHistorique;
    private LocalDateTime dateReservation;
    private LocalDateTime dateConfirmation;
    private LocalDateTime dateAnnulation;
    private String causeAnnulation;
    private String origineAnnulation;
    private double tauxAnnulation;
    private double compensation;
    private UUID idReservation;

    @Transient
    private boolean isNew = false;

    @Override
    public UUID getId() { return idHistorique; }

    @Override
    public boolean isNew() { return isNew || idHistorique == null; }

    public void setAsNew() { this.isNew = true; }
}
