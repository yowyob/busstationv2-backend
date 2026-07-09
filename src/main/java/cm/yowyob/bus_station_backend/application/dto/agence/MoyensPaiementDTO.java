package cm.yowyob.bus_station_backend.application.dto.agence;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record MoyensPaiementDTO(
        @NotEmpty(message = "La liste ne peut pas être vide") List<String> moyensPaiement
) {}