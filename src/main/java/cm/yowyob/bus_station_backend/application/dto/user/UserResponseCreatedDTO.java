package cm.yowyob.bus_station_backend.application.dto.user;

import java.util.List;

import cm.yowyob.bus_station_backend.domain.enums.BusinessActorType;
import cm.yowyob.bus_station_backend.domain.enums.Gender;
import cm.yowyob.bus_station_backend.domain.enums.RoleType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseCreatedDTO {
    private String created_at;
    private String updated_at;
    private String deleted_at;
    private String created_by;
    private String updated_by;
    private String id;
    private String email;
    private String friendly_name;
    private String secondary_email;
    private String date_of_birth;
    private Gender gender;
    private String country_code;
    private String dial_code;
    private String secondary_phone_number;
    private String avatar_picture;
    private String profile_picture;
    private String country_id;
    private String last_login_time;
    private List<String> keywords;
    private String registration_date;
    private BusinessActorType type;
    private String first_name;
    private String last_name;
    private String username;
    private String phone_number;
    private List<RoleType> roles;

}
