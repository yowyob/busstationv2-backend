package cm.yowyob.bus_station_backend.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgenceVoyage {
    private UUID agencyId;
    private UUID organisationId;
    private UUID userId;
    private String longName;
    private String shortName;
    private String location;
    private String socialNetwork;
    private String description;
    private String greetingMessage;

    private UUID gareRoutiereId;
    private Boolean isActive;
    private List<String> moyensPaiement;
    private UUID vehiculeIdDefaut;
    private UUID chauffeurIdDefaut;
}