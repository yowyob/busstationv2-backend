package cm.yowyob.bus_station_backend.infrastructure.inbound.controller;

import cm.yowyob.bus_station_backend.application.dto.payment.PayRequestDTO;
import cm.yowyob.bus_station_backend.application.dto.payment.ResultStatus;
import cm.yowyob.bus_station_backend.application.dto.reservation.*;

import static cm.yowyob.bus_station_backend.infrastructure.util.SecurityUtils.getCurrentUserId;
import cm.yowyob.bus_station_backend.application.dto.voyage.VoyageCancelDTO;
import cm.yowyob.bus_station_backend.application.port.in.AnnulationUseCase;
import cm.yowyob.bus_station_backend.application.port.in.ReservationUseCase;
import cm.yowyob.bus_station_backend.domain.model.Reservation;
import cm.yowyob.bus_station_backend.domain.exception.ResourceNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@AllArgsConstructor
@RequestMapping("/reservation")
@SecurityRequirement(name = "bearerAuth")
@Slf4j
public class ReservationController {

        private final ReservationUseCase reservationUseCase;
        private final AnnulationUseCase annulationUseCase;

        @Operation(summary = "Obtenir toutes les réservations d'un utilisateur")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Liste récupérée avec succès", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ReservationPreviewDTO.class)))),
                        @ApiResponse(responseCode = "400", description = "Données invalides")
        })
        @GetMapping("/user/{userId}")
        public Mono<Page<ReservationPreviewDTO>> getAllReservationsForUser(
                        @PathVariable UUID userId,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size) {
                return reservationUseCase.getReservationsByUser(userId, PageRequest.of(page, size));
        }

        @Operation(summary = "Obtenir toutes les réservations d'une agence")
        @GetMapping("/agence/{agenceId}")
        public Mono<Page<ReservationPreviewDTO>> getAllReservationsForAgence(
                        @PathVariable UUID agenceId,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size) {
                return reservationUseCase.getReservationsByAgence(agenceId, PageRequest.of(page, size));
        }

        @Operation(summary = "Obtenir les détails d'une réservation par son ID")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Réservation trouvée", content = @Content(schema = @Schema(implementation = ReservationDetailDTO.class))),
                        @ApiResponse(responseCode = "404", description = "Réservation introuvable")
        })
        @GetMapping("/{reservationId}")
        public Mono<ResponseEntity<ReservationDetailDTO>> getReservationById(@PathVariable UUID reservationId) {
                return reservationUseCase.getReservationDetails(reservationId)
                                .map(ResponseEntity::ok)
                                .onErrorResume(ResourceNotFoundException.class, e -> Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage())));
        }

        @Operation(summary = "Obtenir toutes les réservations (Admin)")
        @GetMapping
        public Mono<Page<ReservationPreviewDTO>> getAllReservations(
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size) {
                return reservationUseCase.getAllReservations(PageRequest.of(page, size));
        }

        @Operation(summary = "Créer une nouvelle réservation")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "201", description = "Réservation créée", content = @Content(schema = @Schema(implementation = Reservation.class))),
                        @ApiResponse(responseCode = "400", description = "Données invalides")
        })
        @PostMapping("/reserver")
        public Mono<ResponseEntity<Reservation>> createReservation(@RequestBody ReservationDTO reservationDTO) {
                return getCurrentUserId()
                                .flatMap(userId -> {
                                        reservationDTO.setIdUser(userId);
                                        return reservationUseCase.createReservation(reservationDTO);
                                })
                                .map(res -> new ResponseEntity<>(res, HttpStatus.CREATED));
        }

        @Operation(summary = "Confirmer une réservation")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Réservation confirmée", content = @Content(schema = @Schema(implementation = ReservationDetailDTO.class))),
                        @ApiResponse(responseCode = "404", description = "Réservation introuvable")
        })
        @PostMapping("/confirmer")
        public Mono<ResponseEntity<ReservationDetailDTO>> confirmer(@RequestBody PayRequestDTO payRequestDTO) {
                return reservationUseCase.confirmer(payRequestDTO)
                                .map(ResponseEntity::ok)
                                .onErrorResume(ResourceNotFoundException.class, e -> Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage())));
        }

        @Operation(summary = "Annuler une réservation par un utilisateur")
        @PostMapping("/annuler/{reservationId}")
        public Mono<ResponseEntity<Void>> annulerReservation(
                        @PathVariable UUID reservationId,
                        @RequestBody(required = false) ReservationCancelDTO cancelDTO) {
                
                ReservationCancelDTO finalDTO = cancelDTO != null ? cancelDTO : new ReservationCancelDTO();
                finalDTO.setIdReservation(reservationId);
                
                return getCurrentUserId()
                                .flatMap(userId -> annulationUseCase.cancelReservationByUser(finalDTO, userId))
                                .then(Mono.just(ResponseEntity.noContent().build()));
        }

        @Operation(summary = "Annuler un voyage par une agence (avec remboursement/indemnisation)")
        @PostMapping("/agence/annuler-voyage")
        public Mono<ResponseEntity<Void>> agencyCancelVoyage(@RequestBody VoyageCancelDTO cancelDTO) {
                return getCurrentUserId()
                                .flatMap(userId -> annulationUseCase.cancelVoyage(cancelDTO, userId))
                                .then(Mono.just(ResponseEntity.noContent().build()));
        }

        @Operation(summary = "Vérifier le statut d'un paiement")
        @GetMapping("/paiement/status/{transactionCode}")
        public Mono<ResponseEntity<ResultStatus>> getPaymentStatus(@PathVariable String transactionCode) {
                return reservationUseCase.getPaymentStatus(transactionCode)
                                .map(ResponseEntity::ok);
        }
}
