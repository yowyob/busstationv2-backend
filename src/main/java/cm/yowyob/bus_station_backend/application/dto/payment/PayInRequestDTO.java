package cm.yowyob.bus_station_backend.application.dto.payment;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PayInRequestDTO {
    @JsonProperty("payer_phone_number")
    private String payerPhoneNumber;

    @JsonProperty("transaction_amount")
    private double transactionAmount;

    @JsonProperty("transaction_currency")
    private String transactionCurrency; // "XAF"

    @JsonProperty("transaction_method")
    private String transactionMethod; // "MOBILE"

    @JsonProperty("payer_email")
    private String payerEmail;

    @JsonProperty("payer_name")
    private String payerName;

    @JsonProperty("payer_reference")
    private String payerReference;

    @JsonProperty("transaction_reference")
    private String transactionReference;
}
