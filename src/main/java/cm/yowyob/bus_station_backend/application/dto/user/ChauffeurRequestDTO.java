package cm.yowyob.bus_station_backend.application.dto.user;

import cm.yowyob.bus_station_backend.application.validation.OnCreate;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class ChauffeurRequestDTO extends UserDTO{
    @NotNull(message = "L'ID de l'agence de voyage est requis", groups = OnCreate.class)
    private UUID agenceVoyageId;

    private boolean isUserExist = false;
}
