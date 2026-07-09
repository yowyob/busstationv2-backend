package cm.yowyob.bus_station_backend.application.dto.auth;

import cm.yowyob.bus_station_backend.application.dto.user.UserResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthTokensDTO {
    private String accessToken;
    private String refreshToken;
    private long expiresIn;        // durée de validité de l'access token en secondes
    private UserResponseDTO user;
}