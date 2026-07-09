package cm.yowyob.bus_station_backend.application.dto.reservation;

import cm.yowyob.bus_station_backend.domain.enums.PlaceStatus;
import lombok.Data;

@Data
public class PlaceReservationRequest {
    private int placeNumber; // Numéro de la place à réserver
    private PlaceStatus status; // Statut de la place (libre ou réservée)
}