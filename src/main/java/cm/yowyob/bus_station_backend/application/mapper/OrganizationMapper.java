package cm.yowyob.bus_station_backend.application.mapper;

import cm.yowyob.bus_station_backend.application.dto.organization.CreateOrganizationRequest;
import cm.yowyob.bus_station_backend.application.dto.organization.OrganizationDTO;
import cm.yowyob.bus_station_backend.domain.model.Organization;
import org.springframework.stereotype.Component;

@Component
public class OrganizationMapper {

    public Organization toDomain(CreateOrganizationRequest request) {
        if (request == null) {
            return null;
        }

        return Organization.builder()
                .longName(request.getLongName())
                .shortName(request.getShortName())
                .email(request.getEmail())
                .description(request.getDescription())
                .businessDomains(request.getBusinessDomains())
                .logoUrl(request.getLogoUrl())
                .legalForm(request.getLegalForm())
                .websiteUrl(request.getWebsiteUrl())
                .socialNetwork(request.getSocialNetwork())
                .businessRegistrationNumber(request.getBusinessRegistrationNumber())
                .taxNumber(request.getTaxNumber())
                .capitalShare(request.getCapitalShare())
                .registrationDate(request.getRegistrationDate())
                .ceoName(request.getCeoName())
                .yearFounded(request.getYearFounded())
                .keywords(request.getKeywords())
                .createdBy(request.getCreatedBy())
                .build();
    }

    public OrganizationDTO toDTO(Organization domain) {
        if (domain == null) {
            return null;
        }

        OrganizationDTO dto = new OrganizationDTO();
        dto.setOrganizationId(domain.getOrganizationId()); // ou domain.getId() selon votre logique ID
        dto.setCreatedAt(domain.getCreatedAt());
        dto.setUpdatedAt(domain.getUpdatedAt());
        dto.setDeletedAt(domain.getDeletedAt());
        dto.setCreatedBy(domain.getCreatedBy());
        dto.setUpdatedBy(domain.getUpdatedBy());
        dto.setBusinessDomains(domain.getBusinessDomains());
        dto.setEmail(domain.getEmail());
        dto.setShortName(domain.getShortName());
        dto.setLongName(domain.getLongName());
        dto.setDescription(domain.getDescription());
        dto.setLogoUrl(domain.getLogoUrl());
        dto.setIndividualBusiness(domain.isIndividualBusiness());
        dto.setLegalForm(domain.getLegalForm());
        dto.setActive(domain.isActive());
        dto.setWebsiteUrl(domain.getWebsiteUrl());
        dto.setSocialNetwork(domain.getSocialNetwork());
        dto.setBusinessRegistrationNumber(domain.getBusinessRegistrationNumber());
        dto.setTaxNumber(domain.getTaxNumber());
        dto.setCapitalShare(domain.getCapitalShare());
        dto.setRegistrationDate(domain.getRegistrationDate());
        dto.setCeoName(domain.getCeoName());
        dto.setYearFounded(domain.getYearFounded());
        dto.setKeywords(domain.getKeywords());
        dto.setStatus(domain.getStatus());

        return dto;
    }
}