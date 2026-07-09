package cm.yowyob.bus_station_backend.application.dto.payment;

import lombok.Data;

@Data
public class PayInResultDTO {
    ResultStatus status;
    String message;
    PayInData data;
    PayInErrors errors;
    boolean ok;
}
