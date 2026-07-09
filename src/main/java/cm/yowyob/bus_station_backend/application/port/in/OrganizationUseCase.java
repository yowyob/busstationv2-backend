package cm.yowyob.bus_station_backend.application.port.in;

import cm.yowyob.bus_station_backend.application.dto.organization.*;
import cm.yowyob.bus_station_backend.domain.model.AgenceVoyage;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface OrganizationUseCase {
    Mono<OrganizationDTO> createOrganization(CreateOrganizationRequest request);

    Flux<AgenceVoyage> findAllAgenciesByOrganization(UUID organizationId);

    // Récupérer les détails d'une organisation
    Mono<OrganizationDTO> getOrganizationById(UUID organizationId);

    // Mettre à jour une organisation
    Mono<OrganizationDTO> updateOrganization(UUID organizationId, OrganizationDTO organizationDTO);

    Flux<OrganizationDTO> getOrganizationsByUser(UUID userId);
}
