package cm.yowyob.bus_station_backend.application.mapper;

import org.springframework.stereotype.Component;

import cm.yowyob.bus_station_backend.application.dto.taxe.TaxeAffiliationCreateDTO;
import cm.yowyob.bus_station_backend.application.dto.taxe.TaxeAffiliationResponseDTO;
import cm.yowyob.bus_station_backend.application.dto.taxe.TaxeAffiliationUpdateDTO;
import cm.yowyob.bus_station_backend.domain.enums.PolitiqueOuTaxe;
import cm.yowyob.bus_station_backend.domain.model.PolitiqueEtTaxes;

/**
 * Mapper Taxe : la table sous-jacente est `politique_et_taxes` filtrée sur type=TAXE.
 * `nomTaxe` correspond à `nomPolitique` dans le modèle.
 */
@Component
public class TaxeAffiliationMapper {

    public TaxeAffiliationResponseDTO toResponseDTO(PolitiqueEtTaxes domain) {
        if (domain == null) return null;
        return TaxeAffiliationResponseDTO.builder()
                .idTaxe(domain.getIdPolitique())
                .gareRoutiereId(domain.getGareRoutiereId())
                .nomTaxe(domain.getNomPolitique())
                .description(domain.getDescription())
                .tauxTaxe(domain.getTauxTaxe())
                .montantFixe(domain.getMontantFixe())
                .dateEffet(domain.getDateEffet())
                .documentUrl(domain.getDocumentUrl())
                .build();
    }

    public PolitiqueEtTaxes fromCreateDTO(TaxeAffiliationCreateDTO dto) {
        if (dto == null) return null;
        PolitiqueEtTaxes p = new PolitiqueEtTaxes();
        p.setGareRoutiereId(dto.getGareRoutiereId());
        p.setNomPolitique(dto.getNomTaxe());
        p.setDescription(dto.getDescription());
        // null-safe : si null, on stocke 0 (les champs domain sont primitifs double)
        p.setTauxTaxe(dto.getTauxTaxe() != null ? dto.getTauxTaxe() : 0.0);
        p.setMontantFixe(dto.getMontantFixe() != null ? dto.getMontantFixe() : 0.0);
        p.setDateEffet(dto.getDateEffet());
        p.setType(PolitiqueOuTaxe.TAXE);
        return p;
    }

    /**
     * PATCH-like.
     */
    public void applyUpdate(PolitiqueEtTaxes existing, TaxeAffiliationUpdateDTO dto) {
        if (dto == null) return;
        if (dto.getNomTaxe() != null)     existing.setNomPolitique(dto.getNomTaxe());
        if (dto.getDescription() != null) existing.setDescription(dto.getDescription());
        if (dto.getTauxTaxe() != null)    existing.setTauxTaxe(dto.getTauxTaxe());
        if (dto.getMontantFixe() != null) existing.setMontantFixe(dto.getMontantFixe());
        if (dto.getDateEffet() != null)   existing.setDateEffet(dto.getDateEffet());
        // type reste forcément TAXE
        existing.setType(PolitiqueOuTaxe.TAXE);
    }
}
