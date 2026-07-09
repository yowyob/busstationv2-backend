package cm.yowyob.bus_station_backend.application.dto.payment;

import lombok.Data;

@Data
public class PayInData {
    String message;
    int status_code;
    String transaction_code;
    TransactionStatus transaction_status;
}