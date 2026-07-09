package cm.yowyob.bus_station_backend.application.dto.alerte;

import cm.yowyob.bus_station_backend.domain.enums.TypeAlerte;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class AlerteCreateDTO {

    @NotNull(message = "L'identifiant de l'agence est obligatoire")
    private UUID agenceId;

    @NotNull(message = "Le type d'alerte est obligatoire")
    private TypeAlerte type;

    @NotBlank(message = "Le message est obligatoire")
    private String message;
}