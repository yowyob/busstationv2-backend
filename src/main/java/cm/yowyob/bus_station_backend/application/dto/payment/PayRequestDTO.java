package cm.yowyob.bus_station_backend.application.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PayRequestDTO {
    String mobilePhone;
    String mobilePhoneName;
    double amount;
    UUID userId;
    UUID reservationId;
}