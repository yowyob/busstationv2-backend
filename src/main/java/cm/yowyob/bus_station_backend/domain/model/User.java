package cm.yowyob.bus_station_backend.domain.model;

import cm.yowyob.bus_station_backend.domain.enums.BusinessActorType;
import cm.yowyob.bus_station_backend.domain.enums.Gender;
import cm.yowyob.bus_station_backend.domain.enums.RoleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private UUID userId;
    private String nom;
    private String prenom;
    private Gender genre;
    private String username;
    private String email;
    private String password;
    private String telNumber;
    private List<RoleType> roles; // Changé de String à List<RoleType> pour être plus propre dans le domaine
    private BusinessActorType businessActorType;
    private String address;
    private UUID idcoordonneeGPS;

    // Logique métier interne (si nécessaire)
    public boolean isProvider() {
        return this.businessActorType == BusinessActorType.PROVIDER;
    }
}
