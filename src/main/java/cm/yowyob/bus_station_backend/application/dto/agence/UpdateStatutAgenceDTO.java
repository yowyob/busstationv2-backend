package cm.yowyob.bus_station_backend.application.dto.agence;

import jakarta.validation.constraints.NotNull;

public record UpdateStatutAgenceDTO(
        @NotNull(message = "Le champ 'active' est requis") Boolean active,
        String motif
) {}