package cm.yowyob.bus_station_backend.application.dto.user;

import cm.yowyob.bus_station_backend.application.validation.OnCreate;
import cm.yowyob.bus_station_backend.application.validation.OnUpdate;
import cm.yowyob.bus_station_backend.domain.enums.Gender;
import cm.yowyob.bus_station_backend.domain.enums.RoleType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {
    private String last_name;
    private String first_name;

    @NotNull(message = "L'email ne peut pas être null", groups = OnCreate.class)
    @Email(message = "L'email doit être valide", groups = {OnCreate.class, OnUpdate.class})
    private String email;

    @NotNull(message = "Le username ne peut pas être null", groups = OnCreate.class)
    private String username;

    @NotNull(message = "Le password ne peut pas être null", groups = OnCreate.class)
    private String password;

    private String phone_number;

    @NotNull(message = "Le Role ne peut pas être null", groups = OnCreate.class)
    private List<RoleType> role;

    @NotNull(message = "Le genre ne peut pas être null", groups = OnCreate.class)
    private Gender gender;
}