package cm.yowyob.bus_station_backend.application.dto.agence;

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
public class AgenceVoyageResponseDTO {
    private UUID id;
    private UUID organisationId;
    private UUID userId;
    private String longName;
    private String shortName;
    private String logoUrl;
    private String location;
    private String socialNetwork;
    private String description;
    private String greetingMessage;
    private double rating;
    private List<String> specialties;
    private ContactDTO contact;
    private List<UUID> gareIds;
    private Boolean isActive;
    private List<String> moyensPaiement;
    private UUID vehiculeIdDefaut;
    private UUID chauffeurIdDefaut;
}
