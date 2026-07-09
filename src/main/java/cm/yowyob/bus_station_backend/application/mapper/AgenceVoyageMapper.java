package cm.yowyob.bus_station_backend.application.mapper;

import cm.yowyob.bus_station_backend.application.dto.agence.AgenceVoyageDTO;
import cm.yowyob.bus_station_backend.application.dto.agence.AgenceVoyagePreviewDTO;
import cm.yowyob.bus_station_backend.application.dto.agence.AgenceVoyageResponseDTO;
import cm.yowyob.bus_station_backend.application.dto.agence.ContactDTO;
import cm.yowyob.bus_station_backend.domain.model.AgenceVoyage;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Component
public class AgenceVoyageMapper {

    /**
     * Convertit le DTO (entrée API) vers le Modèle du Domaine.
     */
    public AgenceVoyage toDomain(AgenceVoyageDTO dto) {
        if (dto == null) {
            return null;
        }

        return AgenceVoyage.builder()
                .organisationId(dto.getOrganisation_id())
                .userId(dto.getUser_id())
                .longName(dto.getLong_name())
                .shortName(dto.getShort_name())
                .location(dto.getLocation())
                .gareRoutiereId(dto.getGare_routiere_id())
                .socialNetwork(dto.getSocial_network())
                .description(dto.getDescription())
                .greetingMessage(dto.getGreeting_message())
                .build();
    }

    /**
     * Convertit le Modèle du Domaine vers le DTO (sortie API).
     */
    public AgenceVoyageDTO toDTO(AgenceVoyage domain) {
        if (domain == null) {
            return null;
        }

        AgenceVoyageDTO dto = new AgenceVoyageDTO();
        dto.setOrganisation_id(domain.getOrganisationId());
        dto.setUser_id(domain.getUserId());
        dto.setLong_name(domain.getLongName());
        dto.setShort_name(domain.getShortName());
        dto.setLocation(domain.getLocation());
        dto.setGare_routiere_id(domain.getGareRoutiereId());
        dto.setSocial_network(domain.getSocialNetwork());
        dto.setDescription(domain.getDescription());
        dto.setGreeting_message(domain.getGreetingMessage());

        return dto;
    }

    public AgenceVoyageResponseDTO toResponseDTO(AgenceVoyage domain) {
        if (domain == null) {
            return null;
        }

        List<UUID> gareIds = domain.getGareRoutiereId() != null 
            ? List.of(domain.getGareRoutiereId()) 
            : Collections.emptyList();

        return AgenceVoyageResponseDTO.builder()
                .id(domain.getAgencyId())
                .organisationId(domain.getOrganisationId())
                .userId(domain.getUserId())
                .longName(domain.getLongName())
                .shortName(domain.getShortName())
                .logoUrl("/placeholder.svg") // Par défaut pour l'instant
                .location(domain.getLocation())
                .socialNetwork(domain.getSocialNetwork())
                .description(domain.getDescription())
                .greetingMessage(domain.getGreetingMessage())
                .rating(0.0) // Valeur par défaut
                .specialties(Collections.emptyList()) // Liste vide par défaut
                .contact(ContactDTO.builder()
                        .email("")
                        .phone("")
                        .website("")
                        .build())
                .gareIds(gareIds)
                .isActive(domain.getIsActive())
                .moyensPaiement(domain.getMoyensPaiement())
                .vehiculeIdDefaut(domain.getVehiculeIdDefaut())
                .chauffeurIdDefaut(domain.getChauffeurIdDefaut())
                .build();
    }

    public AgenceVoyagePreviewDTO toPreviewDTO(AgenceVoyage agence) {
        if (agence == null) {
            return null;
        }

        AgenceVoyagePreviewDTO previewDTO = new AgenceVoyagePreviewDTO();
        previewDTO.setIdAgenceVoyage(agence.getAgencyId());
        previewDTO.setLongName(agence.getLongName());
        previewDTO.setShortName(agence.getShortName());
        previewDTO.setLocation(agence.getLocation());

        return previewDTO;
    }
}