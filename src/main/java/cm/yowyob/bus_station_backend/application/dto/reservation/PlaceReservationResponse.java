package cm.yowyob.bus_station_backend.application.dto.reservation;

import cm.yowyob.bus_station_backend.domain.enums.PlaceStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlaceReservationResponse {
    private int placeNumber; // Numéro de la place réservée
    private PlaceStatus status; // Statut de la place (libre ou réservée)
}