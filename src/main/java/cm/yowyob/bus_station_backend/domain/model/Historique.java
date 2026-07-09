package cm.yowyob.bus_station_backend.domain.model;

import cm.yowyob.bus_station_backend.domain.enums.StatutHistorique;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Historique {
    private UUID idHistorique;
    private StatutHistorique statusHistorique;
    private LocalDateTime dateReservation;
    private LocalDateTime dateConfirmation;
    private LocalDateTime dateAnnulation;
    private String causeAnnulation;
    private String origineAnnulation;
    private double tauxAnnulation;
    private double compensation;
    private UUID idReservation;
}
