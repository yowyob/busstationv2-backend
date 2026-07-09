package cm.yowyob.bus_station_backend.application.service;

import cm.yowyob.bus_station_backend.application.dto.payment.PaiementCallbackDTO;
import cm.yowyob.bus_station_backend.application.dto.payment.PayInResultDTO;
import cm.yowyob.bus_station_backend.application.dto.payment.PayRequestDTO;
import cm.yowyob.bus_station_backend.application.dto.payment.ResultStatus;
import cm.yowyob.bus_station_backend.application.dto.payment.StatusResultDTO;
import cm.yowyob.bus_station_backend.application.dto.reservation.ReservationConfirmDTO;
import cm.yowyob.bus_station_backend.application.dto.reservation.ReservationDetailDTO;
import cm.yowyob.bus_station_backend.application.port.in.PaiementUseCase;
import cm.yowyob.bus_station_backend.application.port.in.ReservationUseCase;
import cm.yowyob.bus_station_backend.application.port.out.PaymentPort;
import cm.yowyob.bus_station_backend.application.port.out.ReservationPersistencePort;
import cm.yowyob.bus_station_backend.domain.enums.StatutPayment;
import cm.yowyob.bus_station_backend.domain.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaiementService implements PaiementUseCase {

    private static final String GATEWAY_DOWN_MSG =
            "Service de paiement temporairement indisponible. Veuillez réessayer plus tard.";

    private final ReservationUseCase reservationUseCase;
    private final PaymentPort paymentPort;
    private final ReservationPersistencePort reservationPort;

    @Override
    public Mono<PayInResultDTO> initierPaiement(PayRequestDTO request) {
        if (request == null || request.getReservationId() == null) {
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "reservationId est obligatoire"));
        }
        if (request.getMobilePhone() == null || request.getMobilePhone().isBlank()) {
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "mobilePhone est obligatoire"));
        }
        if (request.getAmount() <= 0) {
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "amount doit être > 0"));
        }
        log.info("Initiation paiement reservation={} amount={} phone={}",
                request.getReservationId(), request.getAmount(), request.getMobilePhone());

        return reservationUseCase.initiatePayment(request)
                // On ne capture que les pannes gateway, pas les erreurs métier (404, 400, etc.)
                .onErrorResume(this::isGatewayError, e -> {
                    log.error("Gateway paiement indisponible (initier) : {}", e.toString());
                    return Mono.just(buildPayInFallback());
                });
    }

    @Override
    public Mono<ReservationDetailDTO> confirmerPaiement(PaiementCallbackDTO callback) {
        if (callback == null || callback.getReservationId() == null) {
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "reservationId est obligatoire"));
        }
        log.info("Webhook confirmer paiement transactionCode={} reservationId={}",
                callback.getTransactionCode(), callback.getReservationId());

        return reservationPort.findById(callback.getReservationId())
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Réservation introuvable : " + callback.getReservationId())))
                .flatMap(reservation -> {
                    double montant = callback.getMontantPaye() != null
                            ? callback.getMontantPaye()
                            : reservation.getPrixTotal();
                    if (callback.getTransactionCode() != null
                            && reservation.getTransactionCode() == null) {
                        reservation.setTransactionCode(callback.getTransactionCode());
                    }
                    ReservationConfirmDTO confirmDTO = new ReservationConfirmDTO(
                            reservation.getIdReservation(), montant);
                    return reservationUseCase.confirmReservation(confirmDTO)
                            .flatMap(r -> reservationUseCase.getReservationDetails(
                                    r.getIdReservation()));
                });
    }

    @Override
    public Mono<Void> echecPaiement(PaiementCallbackDTO callback) {
        if (callback == null || callback.getReservationId() == null) {
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "reservationId est obligatoire"));
        }
        log.warn("Webhook echec paiement transactionCode={} reservationId={} motif='{}'",
                callback.getTransactionCode(), callback.getReservationId(), callback.getMotif());

        return reservationPort.findById(callback.getReservationId())
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Réservation introuvable : " + callback.getReservationId())))
                .flatMap(reservation -> {
                    reservation.setStatutPayement(StatutPayment.FAILED);
                    if (callback.getTransactionCode() != null) {
                        reservation.setTransactionCode(callback.getTransactionCode());
                    }
                    return reservationPort.save(reservation).then();
                });
    }

    @Override
    public Mono<StatusResultDTO> verifierStatut(String transactionCode) {
        if (transactionCode == null || transactionCode.isBlank()) {
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "transactionCode est obligatoire"));
        }
        return paymentPort.checkPaymentStatus(transactionCode)
                .onErrorResume(this::isGatewayError, e -> {
                    log.error("Gateway paiement indisponible (statut) : {}", e.toString());
                    return Mono.just(buildStatusFallback());
                });
    }

    // --- Helpers ---

    /**
     * Filtre : on ne traite comme "panne gateway" que ce qui n'est PAS
     * une exception métier qu'on a nous-mêmes lancée (validation, 404, etc.).
     */
    private boolean isGatewayError(Throwable e) {
        return !(e instanceof ResponseStatusException)
                && !(e instanceof ResourceNotFoundException);
    }

    private PayInResultDTO buildPayInFallback() {
        PayInResultDTO dto = new PayInResultDTO();
        dto.setStatus(ResultStatus.ERROR);
        dto.setMessage(GATEWAY_DOWN_MSG);
        dto.setOk(false);
        return dto;
    }

    private StatusResultDTO buildStatusFallback() {
        StatusResultDTO dto = new StatusResultDTO();
        dto.setStatus(ResultStatus.ERROR);
        dto.setMessage(GATEWAY_DOWN_MSG);
        dto.setOk(false);
        return dto;
    }
}
