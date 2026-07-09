package cm.yowyob.bus_station_backend.application.dto.statistics;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.Map;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class AgenceStatisticsDTO {
    private long nombreEmployes;
    private long nombreChauffeurs;
    private long nombreVoyages;
    private Map<String, Long> voyagesParStatut;
    private long nombreReservations;
    private Map<String, Long> reservationsParStatut;
    private double revenus;
    private long nouveauxUtilisateurs;
    private double tauxOccupation; // Pourcentage de places réservées
}