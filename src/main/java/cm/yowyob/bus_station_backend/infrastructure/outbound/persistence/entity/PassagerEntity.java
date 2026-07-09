package cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.entity;

import cm.yowyob.bus_station_backend.domain.enums.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Table("passagers")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PassagerEntity implements Persistable<UUID>  {
    @Id
    private UUID idPassager;
    private String numeroPieceIdentific;
    private String nom;
    private Gender genre;
    private int age;
    private int nbrBaggage;
    private UUID idReservation;
    private int placeChoisis;

    @Transient
    private boolean isNew = false;

    @Override
    public UUID getId() { return idPassager; }

    @Override
    public boolean isNew() { return isNew || idPassager == null; }

    public void setAsNew() { this.isNew = true; }
}
