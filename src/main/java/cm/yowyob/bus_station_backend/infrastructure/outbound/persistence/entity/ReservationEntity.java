package cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.entity;

import cm.yowyob.bus_station_backend.domain.enums.StatutPayment;
import cm.yowyob.bus_station_backend.domain.enums.StatutReservation;
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

@Data
@Table("reservations")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationEntity implements Persistable<UUID> {
    @Id
    private UUID idReservation;
    private LocalDateTime dateReservation;
    private LocalDateTime dateConfirmation;
    private int nbrPassager;
    private double prixTotal;
    private StatutReservation statutReservation;
    private UUID idUser;
    private UUID idVoyage;
    private StatutPayment statutPayement;
    private String transactionCode;
    private double montantPaye;

    @Transient
    private boolean isNew = false;

    @Override
    public UUID getId() { return idReservation; }

    @Override
    public boolean isNew() { return isNew || idReservation == null; }

    public void setAsNew() { this.isNew = true; }
}
