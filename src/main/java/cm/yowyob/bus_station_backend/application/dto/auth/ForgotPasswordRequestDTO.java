package cm.yowyob.bus_station_backend.application.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ForgotPasswordRequestDTO(
        @NotBlank @Email String email
) {}