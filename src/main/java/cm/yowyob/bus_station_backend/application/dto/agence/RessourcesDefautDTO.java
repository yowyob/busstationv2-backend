package cm.yowyob.bus_station_backend.application.dto.agence;

import java.util.UUID;

public record RessourcesDefautDTO(
        UUID vehiculeIdDefaut,
        UUID chauffeurIdDefaut
) {}