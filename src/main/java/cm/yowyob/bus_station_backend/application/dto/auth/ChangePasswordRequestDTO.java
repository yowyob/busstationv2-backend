package cm.yowyob.bus_station_backend.application.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequestDTO(
        @NotBlank String oldPassword,
        @NotBlank @Size(min = 6, message = "Le nouveau mot de passe doit faire au moins 6 caractères") String newPassword
) {}