package cm.yowyob.bus_station_backend.infrastructure.inbound.controller;

import cm.yowyob.bus_station_backend.application.dto.reservation.PlaceReservationRequest;
import cm.yowyob.bus_station_backend.application.dto.reservation.PlaceReservationResponse;
import cm.yowyob.bus_station_backend.application.port.in.ReservationUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ReservationWebSocketController {

    private final ReservationUseCase reservationUseCase;

    /**
     * Gère la sélection/désélection de places en temps réel via WebSocket.
     *
     * Mappe le point de terminaison : /voyage/{voyageId}/reserver
     * Diffuse les mises à jour sur : /topic/voyage.{voyageId}
     *
     * @param voyageId ID du voyage (extrait de la destination)
     * @param request  Contient le numéro de place et le statut (RESERVED/AVAILABLE)
     * @return Une liste de toutes les places actuellement verrouillées temporairement pour ce voyage.
     */
    @MessageMapping("/voyage/{voyageId}/reserver")
    @SendTo("/topic/voyage.{voyageId}")
    public Mono<List<PlaceReservationResponse>> handleReservation(
            @DestinationVariable UUID voyageId,
            PlaceReservationRequest request) {

        log.info("WebSocket: Requête de place [{}] avec le statut [{}] pour le voyage {}",
                request.getPlaceNumber(), request.getStatus(), voyageId);

        // Appelle le service réactif pour mettre à jour le cache des places
        // On collecte le Flux en Liste pour correspondre au format attendu par le frontend original
        return reservationUseCase.handlePlaceSelection(voyageId, request)
                .collectList()
                .doOnError(e -> log.error("Erreur WebSocket lors de la sélection de place: {}", e.getMessage()));
    }
}