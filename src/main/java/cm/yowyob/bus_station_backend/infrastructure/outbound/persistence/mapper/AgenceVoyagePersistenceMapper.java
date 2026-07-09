package cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.mapper;

import cm.yowyob.bus_station_backend.domain.model.AgenceVoyage;
import cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.entity.AgenceVoyageEntity;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class AgenceVoyagePersistenceMapper {

    private final ObjectMapper objectMapper;

    public AgenceVoyage toDomain(AgenceVoyageEntity entity) {
        if (entity == null) return null;

        return AgenceVoyage.builder()
                .agencyId(entity.getAgencyId())
                .organisationId(entity.getOrganisationId())
                .userId(entity.getUserId())
                .longName(entity.getLongName())
                .shortName(entity.getShortName())
                .location(entity.getLocation())
                .socialNetwork(entity.getSocialNetwork())
                .description(entity.getDescription())
                .greetingMessage(entity.getGreetingMessage())
                .gareRoutiereId(entity.getGareRoutiereId())
                .isActive(entity.getIsActive())
                .moyensPaiement(parseMoyensPaiement(entity.getMoyensPaiement()))
                .vehiculeIdDefaut(entity.getVehiculeIdDefaut())
                .chauffeurIdDefaut(entity.getChauffeurIdDefaut())
                .build();
    }

    public AgenceVoyageEntity toEntity(AgenceVoyage domain) {
        if (domain == null) return null;

        return AgenceVoyageEntity.builder()
                .agencyId(domain.getAgencyId())
                .organisationId(domain.getOrganisationId())
                .userId(domain.getUserId())
                .longName(domain.getLongName())
                .shortName(domain.getShortName())
                .location(domain.getLocation())
                .socialNetwork(domain.getSocialNetwork())
                .description(domain.getDescription())
                .greetingMessage(domain.getGreetingMessage())
                .gareRoutiereId(domain.getGareRoutiereId())
                .isActive(domain.getIsActive())
                .moyensPaiement(serializeMoyensPaiement(domain.getMoyensPaiement()))
                .vehiculeIdDefaut(domain.getVehiculeIdDefaut())
                .chauffeurIdDefaut(domain.getChauffeurIdDefaut())
                .build();
    }

    private List<String> parseMoyensPaiement(String json) {
        if (json == null || json.isBlank()) return Collections.emptyList();
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            log.warn("Impossible de parser moyens_paiement: {}", json, e);
            return Collections.emptyList();
        }
    }

    private String serializeMoyensPaiement(List<String> list) {
        if (list == null || list.isEmpty()) return null;
        try {
            return objectMapper.writeValueAsString(list);
        } catch (Exception e) {
            log.warn("Impossible de sérialiser moyens_paiement", e);
            return null;
        }
    }
}