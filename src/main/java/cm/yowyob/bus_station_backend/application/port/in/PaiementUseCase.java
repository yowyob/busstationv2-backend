package cm.yowyob.bus_station_backend.application.port.in;

import cm.yowyob.bus_station_backend.application.dto.payment.PaiementCallbackDTO;
import cm.yowyob.bus_station_backend.application.dto.payment.PayInResultDTO;
import cm.yowyob.bus_station_backend.application.dto.payment.PayRequestDTO;
import cm.yowyob.bus_station_backend.application.dto.payment.StatusResultDTO;
import cm.yowyob.bus_station_backend.application.dto.reservation.ReservationDetailDTO;
import reactor.core.publisher.Mono;

public interface PaiementUseCase {
    Mono<PayInResultDTO> initierPaiement(PayRequestDTO request);
    Mono<ReservationDetailDTO> confirmerPaiement(PaiementCallbackDTO callback);
    Mono<Void> echecPaiement(PaiementCallbackDTO callback);
    Mono<StatusResultDTO> verifierStatut(String transactionCode);
}