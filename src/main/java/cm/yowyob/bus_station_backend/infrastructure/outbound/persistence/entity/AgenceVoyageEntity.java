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

@Table("agences_voyage")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgenceVoyageEntity implements Persistable<UUID> {
    @Id
    private UUID agencyId;
    @Column("organisation_id")
    private UUID organisationId;
    @Column("user_id")
    private UUID userId; // ID du ched d'agence
    @Column("name")
    private String longName;
    @Column("short_name")
    private String shortName;
    private String location;
    private String socialNetwork;
    private String description;
    private String greetingMessage;

    @Column("gare_routiere_id")
    private UUID gareRoutiereId;

    @Column("is_active")
    private Boolean isActive;

    @Column("moyens_paiement")
    private String moyensPaiement; // stocké en JSON string

    @Column("vehicule_id_defaut")
    private UUID vehiculeIdDefaut;

    @Column("chauffeur_id_defaut")
    private UUID chauffeurIdDefaut;

    @Transient
    private boolean isNew = false;

    @Override
    public UUID getId() {
        return agencyId;
    }

    @Override
    public boolean isNew() {
        return isNew || agencyId == null;
    }

    public void setAsNew() {
        this.isNew = true;
    }
}
