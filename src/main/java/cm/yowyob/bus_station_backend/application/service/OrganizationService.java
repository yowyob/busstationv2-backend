package cm.yowyob.bus_station_backend.application.service;

import cm.yowyob.bus_station_backend.application.dto.organization.CreateOrganizationRequest;
import cm.yowyob.bus_station_backend.application.dto.organization.OrganizationDTO;
import cm.yowyob.bus_station_backend.application.mapper.OrganizationMapper;
import cm.yowyob.bus_station_backend.application.port.in.OrganizationUseCase;
import cm.yowyob.bus_station_backend.application.port.out.AgencePersistencePort;
import cm.yowyob.bus_station_backend.application.port.out.OrganizationPersistencePort;
import cm.yowyob.bus_station_backend.domain.exception.ResourceNotFoundException;
import cm.yowyob.bus_station_backend.domain.model.AgenceVoyage;
import cm.yowyob.bus_station_backend.domain.model.Organization;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrganizationService implements OrganizationUseCase {

    private final OrganizationPersistencePort organizationPersistencePort;
    private final AgencePersistencePort agencePersistencePort;
    private final OrganizationMapper organizationMapper;

    @Override
    public Mono<OrganizationDTO> createOrganization(CreateOrganizationRequest request) {
        log.info("Création d'une nouvelle organisation : {}", request.getLongName());

        Organization organization = organizationMapper.toDomain(request);

        // Logique de l'ancien backend : Initialisation des champs obligatoires
        organization.setOrganizationId(UUID.randomUUID());
        organization.setCreatedAt(LocalDateTime.now());
        organization.setUpdatedAt(LocalDateTime.now());
        organization.setCreatedBy(request.getCreatedBy());

        // Valeurs par défaut
        organization.setActive(true);
        organization.setStatus("ACTIVE");
        organization.setIndividualBusiness(false);

        return organizationPersistencePort.save(organization)
                .map(organizationMapper::toDTO)
                .doOnSuccess(org -> log.info("Organisation créée avec succès : {}", org.getOrganizationId()));
    }

    @Override
    public Flux<AgenceVoyage> findAllAgenciesByOrganization(UUID organizationId) {
        log.info("Récupération de toutes les agences pour l'organisation : {}", organizationId);
        return agencePersistencePort.findByOrganisationId(organizationId);
    }

    @Override
    public Mono<OrganizationDTO> getOrganizationById(UUID organizationId) {
        return organizationPersistencePort.findByOrganisationId(organizationId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Organisation non trouvée avec l'ID : " + organizationId)))
                .map(organizationMapper::toDTO);
    }

    @Override
    public Flux<OrganizationDTO> getOrganizationsByUser(UUID userId) {
        return organizationPersistencePort.findByCreatedBy(userId)
                .map(organizationMapper::toDTO);
    }

    @Override
    public Mono<OrganizationDTO> updateOrganization(UUID organizationId, OrganizationDTO organizationDTO) {
        log.info("Mise à jour de l'organisation : {}", organizationId);

        return organizationPersistencePort.findByOrganisationId(organizationId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Organisation non trouvée")))
                .flatMap(existingOrg -> {
                    // Mise à jour des champs à partir du DTO
                    existingOrg.setLongName(organizationDTO.getLongName());
                    existingOrg.setShortName(organizationDTO.getShortName());
                    existingOrg.setEmail(organizationDTO.getEmail());
                    existingOrg.setDescription(organizationDTO.getDescription());
                    existingOrg.setLogoUrl(organizationDTO.getLogoUrl());
                    existingOrg.setLegalForm(organizationDTO.getLegalForm());
                    existingOrg.setWebsiteUrl(organizationDTO.getWebsiteUrl());
                    existingOrg.setSocialNetwork(organizationDTO.getSocialNetwork());
                    existingOrg.setBusinessRegistrationNumber(organizationDTO.getBusinessRegistrationNumber());
                    existingOrg.setTaxNumber(organizationDTO.getTaxNumber());
                    existingOrg.setCapitalShare(organizationDTO.getCapitalShare());
                    existingOrg.setCeoName(organizationDTO.getCeoName());
                    existingOrg.setKeywords(organizationDTO.getKeywords());
                    existingOrg.setBusinessDomains(organizationDTO.getBusinessDomains());

                    existingOrg.setUpdatedAt(LocalDateTime.now());
                    existingOrg.setUpdatedBy(organizationDTO.getUpdatedBy());

                    return organizationPersistencePort.save(existingOrg);
                })
                .map(organizationMapper::toDTO);
    }
}