package cm.yowyob.bus_station_backend.application.dto.payment;

import lombok.Data;

@Data
public class StatusResultDTO {
    ResultStatus status;
    String message;
    StatusData data;
    String errors;
    boolean ok;
}
