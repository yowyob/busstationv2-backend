package cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Table("coordonnee")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoordonneeEntity implements Persistable<UUID> {
    @Id
    @Column("id")
    private UUID idCoordonnee;
    private Double latitude;
    private Double longitude;
    private String altitude;

    @Transient
    private boolean isNew = false;

    @Override
    public UUID getId() { return idCoordonnee; }

    @Override
    public boolean isNew() { return isNew || idCoordonnee == null; }

    public void setAsNew() { this.isNew = true; }
}
