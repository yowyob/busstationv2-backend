package cm.yowyob.bus_station_backend.application.port.out;

import cm.yowyob.bus_station_backend.application.dto.payment.PayInResultDTO;
import cm.yowyob.bus_station_backend.application.dto.payment.StatusResultDTO;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface PaymentPort {
    Mono<PayInResultDTO> initiatePayment(String phone, String phoneName, double amount, UUID userId);
    Mono<StatusResultDTO> checkPaymentStatus(String transactionCode);
}
