package cm.yowyob.bus_station_backend.application.mapper;

import org.springframework.stereotype.Component;

import cm.yowyob.bus_station_backend.application.dto.affiliation.AffiliationResponseDTO;
import cm.yowyob.bus_station_backend.application.dto.affiliation.AffiliationUpdateDTO;
import cm.yowyob.bus_station_backend.domain.model.AffiliationAgenceVoyage;

@Component
public class AffiliationMapper {

    public AffiliationResponseDTO toResponseDTO(AffiliationAgenceVoyage domain) {
        if (domain == null) return null;
        return AffiliationResponseDTO.builder()
                .id(domain.getId())
                .gareRoutiereId(domain.getGareRoutiereId())
                .agencyId(domain.getAgencyId())
                .agencyName(domain.getAgencyName())
                .statut(domain.getStatut())
                .echeance(domain.getEcheance())
                .montantAffiliation(domain.getMontantAffiliation())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }

    /**
     * PATCH-like : applique uniquement les champs non-null du DTO à l'entité existante.
     */
    public void applyUpdate(AffiliationAgenceVoyage existing, AffiliationUpdateDTO dto) {
        if (dto == null) return;
        if (dto.getAgencyName() != null)         existing.setAgencyName(dto.getAgencyName());
        if (dto.getMontantAffiliation() != null) existing.setMontantAffiliation(dto.getMontantAffiliation());
        if (dto.getEcheance() != null)           existing.setEcheance(dto.getEcheance());
    }
}
