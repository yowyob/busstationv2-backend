package cm.yowyob.bus_station_backend.application.dto.voyage.generation;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GenerationSemaineRequestDTO {
    @NotNull(message = "agenceId est obligatoire")
    private UUID agenceId;

    @NotEmpty(message = "lignesIds doit contenir au moins une ligne")
    private List<UUID> lignesIds;

    @NotNull(message = "semaineDebut est obligatoire (lundi de la semaine, YYYY-MM-DD)")
    private LocalDate semaineDebut;
}