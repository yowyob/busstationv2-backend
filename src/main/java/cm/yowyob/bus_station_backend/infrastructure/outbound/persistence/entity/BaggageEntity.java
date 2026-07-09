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

@Table("baggages")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BaggageEntity implements Persistable<UUID> {
    @Id
    private UUID idBaggage;
    private String nbreBaggage;
    private UUID idPassager;

    @Transient
    private boolean isNew = false;

    @Override
    public UUID getId() { return idBaggage; }

    @Override
    public boolean isNew() { return isNew || idBaggage == null; }

    public void setAsNew() { this.isNew = true; }
}
