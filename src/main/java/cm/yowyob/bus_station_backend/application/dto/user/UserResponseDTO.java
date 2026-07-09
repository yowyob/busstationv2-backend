package cm.yowyob.bus_station_backend.application.dto.user;

import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

import cm.yowyob.bus_station_backend.domain.enums.RoleType;
import cm.yowyob.bus_station_backend.domain.model.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDTO {
    private UUID userId;
    private String token;
    @JsonProperty("last_name")
    private String lastName;
    @JsonProperty("first_name")
    private String firstName;
    private String email;
    private String username;
    @JsonProperty("phone_number")
    private String phoneNumber;
    private List<RoleType> role;

    public static UserResponseDTO fromUser(User user) {
        UserResponseDTO userResponseDTO = new UserResponseDTO();
        userResponseDTO.setUsername(user.getUsername());
        userResponseDTO.setLastName(user.getNom());
        userResponseDTO.setFirstName(user.getPrenom());
        userResponseDTO.setEmail(user.getEmail());
        userResponseDTO.setUserId(user.getUserId());
        userResponseDTO.setRole(user.getRoles());
        userResponseDTO.setPhoneNumber(user.getTelNumber());

        return userResponseDTO;
    }
}