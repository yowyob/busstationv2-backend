package cm.yowyob.bus_station_backend.infrastructure.outbound.external;

import cm.yowyob.bus_station_backend.application.dto.payment.PayInRequestDTO;
import cm.yowyob.bus_station_backend.application.dto.payment.PayInResultDTO;
import cm.yowyob.bus_station_backend.application.dto.payment.StatusResultDTO;
import cm.yowyob.bus_station_backend.application.port.out.PaymentPort;
import cm.yowyob.bus_station_backend.application.port.out.UserPersistencePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentWebClientAdapter implements PaymentPort {

        private final WebClient webClient;
        private final UserPersistencePort userPersistencePort; // Nécessaire pour récupérer l'email

        @Value("${payment.apiKey}")
        private String apiKey;

        @Value("${payment.baseUrl:https://gateway.yowyob.com/payment-service}")
        private String baseUrl;

        @Override
        public Mono<PayInResultDTO> initiatePayment(String phone, String phoneName, double amount, UUID userId) {

                // Récupération réactive de l'utilisateur pour avoir son email
                return userPersistencePort.findById(userId)
                                .switchIfEmpty(Mono.error(
                                                new RuntimeException("Utilisateur introuvable pour le paiement")))
                                .flatMap(user -> {
                                        // Construction du Payload
                                        PayInRequestDTO request = PayInRequestDTO.builder()
                                                        .payerPhoneNumber(phone)
                                                        .payerName(phoneName)
                                                        .transactionAmount(amount)
                                                        .transactionCurrency("XAF")
                                                        .transactionMethod("MOBILE")
                                                        .payerEmail(user.getEmail())
                                                        .payerReference(user.getUserId().toString())
                                                        .transactionReference(UUID.randomUUID().toString())
                                                        .build();

                                        // 3. Appel WebClient non-bloquant
                                        return webClient.post()
                                                        .uri(baseUrl + "/" + apiKey + "/payin")
                                                        .contentType(MediaType.APPLICATION_JSON)
                                                        .bodyValue(request)
                                                        .retrieve()
                                                        // Gestion basique des erreurs 4xx/5xx
                                                        .onStatus(status -> status.is4xxClientError()
                                                                        || status.is5xxServerError(),
                                                                        clientResponse -> Mono
                                                                                        .error(new RuntimeException(
                                                                                                        "Erreur API Paiement: "
                                                                                                                        + clientResponse.statusCode())))
                                                        .bodyToMono(PayInResultDTO.class)
                                                        .doOnSuccess(res -> log.info("Paiement initié avec succès: {}",
                                                                        res.getData().getTransaction_code()))
                                                        .doOnError(e -> log.error("Échec de l'initiation du paiement",
                                                                        e));
                                });
        }

        @Override
        public Mono<StatusResultDTO> checkPaymentStatus(String transactionCode) {
                return webClient.get()
                                .uri(baseUrl + "/" + apiKey + "/transactions/" + transactionCode + "/status")
                                .retrieve()
                                .bodyToMono(StatusResultDTO.class)
                                .doOnError(e -> log.error("Impossible de vérifier le statut du paiement {}",
                                                transactionCode, e));
        }
}