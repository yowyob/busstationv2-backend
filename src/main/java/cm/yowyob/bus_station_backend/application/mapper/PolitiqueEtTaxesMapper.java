package cm.yowyob.bus_station_backend.application.mapper;

import cm.yowyob.bus_station_backend.application.dto.politique.PolitiqueEtTaxesDTO;
import cm.yowyob.bus_station_backend.application.dto.politique.PolitiqueEtTaxesRequestDTO;
import cm.yowyob.bus_station_backend.domain.model.PolitiqueEtTaxes;
import org.springframework.stereotype.Component;

@Component
public class PolitiqueEtTaxesMapper {

    public PolitiqueEtTaxesDTO toDTO(PolitiqueEtTaxes domain) {
        if (domain == null) {
            return null;
        }
        return PolitiqueEtTaxesDTO.builder()
                .idPolitique(domain.getIdPolitique())
                .gareRoutiereId(domain.getGareRoutiereId())
                .nomPolitique(domain.getNomPolitique())
                .description(domain.getDescription())
                .tauxTaxe(domain.getTauxTaxe())
                .montantFixe(domain.getMontantFixe())
                .dateEffet(domain.getDateEffet())
                .documentUrl(domain.getDocumentUrl())
                .type(domain.getType())
                .build();
    }

    public PolitiqueEtTaxes toDomain(PolitiqueEtTaxesDTO dto) {
        if (dto == null) {
            return null;
        }
        PolitiqueEtTaxes politiqueEtTaxes = new PolitiqueEtTaxes();
        politiqueEtTaxes.setIdPolitique(dto.getIdPolitique());
        politiqueEtTaxes.setGareRoutiereId(dto.getGareRoutiereId());
        politiqueEtTaxes.setNomPolitique(dto.getNomPolitique());
        politiqueEtTaxes.setDescription(dto.getDescription());
        politiqueEtTaxes.setTauxTaxe(dto.getTauxTaxe());
        politiqueEtTaxes.setMontantFixe(dto.getMontantFixe());
        politiqueEtTaxes.setDateEffet(dto.getDateEffet());
        politiqueEtTaxes.setDocumentUrl(dto.getDocumentUrl());
        politiqueEtTaxes.setType(dto.getType());
        return politiqueEtTaxes;
    }

    public PolitiqueEtTaxes fromRequestDTO(PolitiqueEtTaxesRequestDTO dto) {
        if (dto == null) {
            return null;
        }
        PolitiqueEtTaxes politiqueEtTaxes = new PolitiqueEtTaxes();
        politiqueEtTaxes.setGareRoutiereId(dto.getGareRoutiereId());
        politiqueEtTaxes.setNomPolitique(dto.getNomPolitique());
        politiqueEtTaxes.setDescription(dto.getDescription());
        politiqueEtTaxes.setTauxTaxe(dto.getTauxTaxe());
        politiqueEtTaxes.setMontantFixe(dto.getMontantFixe());
        politiqueEtTaxes.setDateEffet(dto.getDateEffet());
        politiqueEtTaxes.setType(dto.getType());
        return politiqueEtTaxes;
    }
}
