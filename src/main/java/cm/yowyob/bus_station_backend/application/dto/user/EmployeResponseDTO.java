package cm.yowyob.bus_station_backend.application.dto.user;

import cm.yowyob.bus_station_backend.domain.enums.StatutEmploye;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class EmployeResponseDTO {
    private UUID employeId;
    private UUID userId;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String poste;
    private String departement;
    private LocalDateTime dateEmbauche;
    private StatutEmploye statutEmploye;
    private String nomManager; // Nom du manager si présent
    private UUID agenceVoyageId;
    private String nomAgence;
}
