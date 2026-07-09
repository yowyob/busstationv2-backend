package cm.yowyob.bus_station_backend.application.port.in;

import cm.yowyob.bus_station_backend.application.dto.payment.PayInResultDTO;
import cm.yowyob.bus_station_backend.application.dto.payment.PayRequestDTO;
import cm.yowyob.bus_station_backend.application.dto.reservation.*;
import cm.yowyob.bus_station_backend.domain.model.Reservation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface ReservationUseCase {
    // Lecture
    Mono<ReservationDetailDTO> getReservationDetails(UUID reservationId);

    Mono<Page<ReservationPreviewDTO>> getReservationsByUser(UUID userId, Pageable pageable);

    Mono<Page<ReservationPreviewDTO>> getReservationsByAgence(UUID agenceId, Pageable pageable);

    // Correspond à findAll (Admin)
    Mono<Page<ReservationPreviewDTO>> getAllReservations(Pageable pageable);

    // Actions Transactionnelles
    Mono<Reservation> createReservation(ReservationDTO reservationDTO);

    Mono<ReservationDetailDTO> confirmer(PayRequestDTO payRequestDTO);
    Mono<cm.yowyob.bus_station_backend.application.dto.payment.ResultStatus> getPaymentStatus(String transactionCode);

    Mono<Reservation> confirmReservation(ReservationConfirmDTO confirmDTO);

    // Paiement
    Mono<PayInResultDTO> initiatePayment(PayRequestDTO payRequestDTO);

    Mono<Void> processPaymentStatusCheck(); // Pour le Scheduled task

    // Billetterie
    Mono<BilletDTO> generateBillet(UUID passagerId);

    // --- Gestion des Places (Temps réel) ---
    // Correspond à reservePlace (retourne Flux pour le streaming
    // WebSocket/Reactive)
    Flux<PlaceReservationResponse> handlePlaceSelection(UUID voyageId, PlaceReservationRequest request);

    // Pour charger l'état initial des places à l'ouverture de la page
    Flux<Integer> getOccupiedAndReservedPlaces(UUID voyageId);

    // Passager
    Flux<PassagerDTO> getAllPassagersByReservation();

    Mono<PassagerDTO> getPassagerById(UUID passagerId);

    Mono<PassagerDTO> updatePassager(UUID passagerId, PassagerDTO dto);

    Mono<Void> deletePassager(UUID passagerId);

    // Baggage
    Flux<BaggageDTO> getAllBagagesByReservation();

    Mono<BaggageDTO> getBaggageById(UUID baggageId);

    Mono<BaggageDTO> createBaggage(BaggageDTO dto);

    Mono<BaggageDTO> updateBaggage(UUID baggageId, BaggageDTO dto);

    Mono<Void> deleteBaggage(UUID baggageId);
}
