package cm.yowyob.bus_station_backend.application.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequestDTO(
        @NotBlank(message = "refreshToken est requis") String refreshToken
) {}