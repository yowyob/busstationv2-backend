package cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.entity;

import cm.yowyob.bus_station_backend.domain.enums.BusinessActorType;
import cm.yowyob.bus_station_backend.domain.enums.Gender;
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

@Table("users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity implements Persistable<UUID> {
    @Id
    @Column("user_id")
    private UUID userId;
    private String nom;
    private String prenom;
    private Gender genre;
    private String username;
    private String email;
    private String password;
    @Column("tel_number")
    private String telNumber;
    private String roles; // Stocké en CSV ou JSONB dans Postgres
    @Column("business_actor_type")
    private BusinessActorType businessActorType;
    private String address;
    @Column("idcoordonnee_gps")
    private UUID idcoordonneeGPS;

    @Transient
    private boolean isNew = false;

    @Override
    public UUID getId() { return userId; }

    @Override
    public boolean isNew() { return isNew || userId == null; }

    public void setAsNew() { this.isNew = true; }
}
