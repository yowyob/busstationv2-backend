package cm.yowyob.bus_station_backend.application.mapper;

import org.springframework.stereotype.Component;

import cm.yowyob.bus_station_backend.application.dto.politiquegare.PolitiqueGareCreateDTO;
import cm.yowyob.bus_station_backend.application.dto.politiquegare.PolitiqueGareResponseDTO;
import cm.yowyob.bus_station_backend.application.dto.politiquegare.PolitiqueGareUpdateDTO;
import cm.yowyob.bus_station_backend.domain.enums.PolitiqueOuTaxe;
import cm.yowyob.bus_station_backend.domain.model.PolitiqueEtTaxes;

/**
 * Mapper Politique Gare : la table sous-jacente est `politique_et_taxes` filtrée sur type=POLITIQUE.
 * Le champ `montant` du DTO correspond à `montantFixe` du domaine.
 */
@Component
public class PolitiqueGareMapper {

    public PolitiqueGareResponseDTO toResponseDTO(PolitiqueEtTaxes domain) {
        if (domain == null) return null;
        return PolitiqueGareResponseDTO.builder()
                .idPolitique(domain.getIdPolitique())
                .gareRoutiereId(domain.getGareRoutiereId())
                .titre(domain.getNomPolitique())
                .description(domain.getDescription())
                .montant(domain.getMontantFixe())
                .dateEffet(domain.getDateEffet())
                .documentUrl(domain.getDocumentUrl())
                .build();
    }

    public PolitiqueEtTaxes fromCreateDTO(PolitiqueGareCreateDTO dto) {
        if (dto == null) return null;
        PolitiqueEtTaxes p = new PolitiqueEtTaxes();
        p.setGareRoutiereId(dto.getGareRoutiereId());
        p.setNomPolitique(dto.getTitre());
        p.setDescription(dto.getDescription());
        p.setMontantFixe(dto.getMontant() != null ? dto.getMontant() : 0.0);
        p.setTauxTaxe(0.0);
        p.setDateEffet(dto.getDateEffet());
        p.setType(PolitiqueOuTaxe.POLITIQUE);
        return p;
    }

    public void applyUpdate(PolitiqueEtTaxes existing, PolitiqueGareUpdateDTO dto) {
        if (dto == null) return;
        if (dto.getTitre() != null)       existing.setNomPolitique(dto.getTitre());
        if (dto.getDescription() != null) existing.setDescription(dto.getDescription());
        if (dto.getMontant() != null)     existing.setMontantFixe(dto.getMontant());
        if (dto.getDateEffet() != null)   existing.setDateEffet(dto.getDateEffet());
        // Type reste POLITIQUE
        existing.setType(PolitiqueOuTaxe.POLITIQUE);
    }
}
