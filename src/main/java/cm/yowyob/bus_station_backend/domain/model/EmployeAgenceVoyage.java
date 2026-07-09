package cm.yowyob.bus_station_backend.domain.model;

import cm.yowyob.bus_station_backend.domain.enums.StatutEmploye;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeAgenceVoyage {
    private UUID employeId;
    private UUID agenceVoyageId;
    private UUID userId;
    private String poste; // Titre du poste (ex: "Responsable Commercial", "Assistant", etc.)
    private LocalDateTime dateEmbauche;
    private LocalDateTime dateFinContrat; // null si toujours actif
    private StatutEmploye statutEmploye;
    private Double salaire; // optionnel
    private String departement; // ex: "Commercial", "Administration", "Maintenance"
    private UUID managerId; // ID du supérieur hiérarchique (optionnel)
}
