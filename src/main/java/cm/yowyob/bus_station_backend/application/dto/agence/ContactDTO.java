package cm.yowyob.bus_station_backend.application.dto.agence;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContactDTO {
    private String email;
    private String phone;
    private String website;
}
