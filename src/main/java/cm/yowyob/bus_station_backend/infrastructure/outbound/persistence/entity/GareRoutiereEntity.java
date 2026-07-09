package cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.annotation.Version;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Table("gare_routiere")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GareRoutiereEntity implements Persistable<UUID> {

    @Id
    @Column("id_gare_routiere")
    private UUID idGareRoutiere;

    @Version
    private Long version;

    private String nomGareRoutiere;

    private String adresse;
    private String ville;
    private String quartier;
    private String description;
    private String services;
    private String horaires;

    private String photoUrl;

    private String nomPresident;

    @Column("id_coordonnee_gps")
    private UUID idCoordonneeGPS;

    @Column("manager_id")
    private UUID managerId;

    @Transient
    private boolean isNew = false;

    @Override
    public UUID getId() {
        return idGareRoutiere;
    }

    @Override
    public boolean isNew() {
        return isNew || version == null;
    }

    public void setAsNew() {
        this.isNew = true;
    }
}
