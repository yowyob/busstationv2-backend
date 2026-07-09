package cm.yowyob.bus_station_backend.application.dto.politiquegare;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * PATCH-like pour `PUT /politique-gare/{id}`. Champs nullables.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PolitiqueGareUpdateDTO {
    private String titre;
    private String description;
    private Double montant;
    private LocalDate dateEffet;
}
