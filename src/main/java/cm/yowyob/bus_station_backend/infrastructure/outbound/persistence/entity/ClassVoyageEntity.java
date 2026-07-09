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

@Table("class_voyage")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassVoyageEntity implements Persistable<UUID> {
    @Id
    @Column("id")
    private UUID id;

    @Column("label")
    private String nom;

    @Column("price")
    private double prix;

    @Column("version")
    private Integer version;

    @Column("is_active")
    private boolean isActive;

    @Column("id_agence_voyage")
    private UUID idAgenceVoyage;

    @Transient
    private boolean isNew = false;

    @Override
    public UUID getId() { return id; }

    @Override
    public boolean isNew() { return isNew || id == null; }

    public void setAsNew() { this.isNew = true; }
}
