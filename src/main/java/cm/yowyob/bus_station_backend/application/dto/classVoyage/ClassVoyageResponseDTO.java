package cm.yowyob.bus_station_backend.application.dto.classVoyage;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ClassVoyageResponseDTO {
    private UUID id;
    private String nom;
    private double prix;
    private UUID idAgenceVoyage;
}
