package cm.yowyob.bus_station_backend.application.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequestDTO(
        @NotBlank String token,
        @NotBlank @Size(min = 6) String newPassword
) {}