package cm.yowyob.bus_station_backend.infrastructure.inbound.controller;

import cm.yowyob.bus_station_backend.application.dto.payment.PaiementCallbackDTO;
import cm.yowyob.bus_station_backend.application.dto.payment.PayInResultDTO;
import cm.yowyob.bus_station_backend.application.dto.payment.PayRequestDTO;
import cm.yowyob.bus_station_backend.application.dto.payment.StatusResultDTO;
import cm.yowyob.bus_station_backend.application.dto.reservation.ReservationDetailDTO;
import cm.yowyob.bus_station_backend.application.port.in.PaiementUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/paiement")
@Slf4j
public class PaiementController {

    private final PaiementUseCase paiementUseCase;

    @Operation(summary = "Initier un paiement mobile money pour une réservation")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Paiement initié",
                    content = @Content(schema = @Schema(implementation = PayInResultDTO.class))),
            @ApiResponse(responseCode = "400", description = "Données invalides"),
            @ApiResponse(responseCode = "404", description = "Réservation introuvable")
    })
    @PostMapping("/initier")
    @SecurityRequirement(name = "bearerAuth")
    public Mono<ResponseEntity<PayInResultDTO>> initier(@RequestBody PayRequestDTO request) {
        return paiementUseCase.initierPaiement(request)
                .map(ResponseEntity::ok);
    }

    @Operation(summary = "Webhook : callback de confirmation de paiement par la gateway",
            description = "Endpoint public appelé par la gateway de paiement. Confirme la réservation et passe le statut paiement à PAID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Réservation confirmée",
                    content = @Content(schema = @Schema(implementation = ReservationDetailDTO.class))),
            @ApiResponse(responseCode = "404", description = "Réservation introuvable")
    })
    @PostMapping("/confirmer")
    public Mono<ResponseEntity<ReservationDetailDTO>> confirmer(@RequestBody PaiementCallbackDTO callback) {
        return paiementUseCase.confirmerPaiement(callback)
                .map(ResponseEntity::ok);
    }

    @Operation(summary = "Webhook : callback d'échec de paiement par la gateway",
            description = "Endpoint public appelé par la gateway de paiement. Passe le statut paiement à FAILED.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Échec enregistré"),
            @ApiResponse(responseCode = "404", description = "Réservation introuvable")
    })
    @PostMapping("/echec")
    public Mono<ResponseEntity<Void>> echec(@RequestBody PaiementCallbackDTO callback) {
        return paiementUseCase.echecPaiement(callback)
                .then(Mono.fromCallable(() -> ResponseEntity.status(HttpStatus.NO_CONTENT).<Void>build()));
    }

    @Operation(summary = "Vérifier le statut d'une transaction de paiement")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Statut récupéré",
                    content = @Content(schema = @Schema(implementation = StatusResultDTO.class))),
            @ApiResponse(responseCode = "400", description = "transactionCode invalide")
    })
    @GetMapping("/statut/{transactionCode}")
    @SecurityRequirement(name = "bearerAuth")
    public Mono<ResponseEntity<StatusResultDTO>> statut(@PathVariable String transactionCode) {
        return paiementUseCase.verifierStatut(transactionCode)
                .map(ResponseEntity::ok);
    }
}