package cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.mapper;

import cm.yowyob.bus_station_backend.domain.model.PolitiqueEtTaxes;
import cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.entity.PolitiqueEtTaxesEntity;
import org.springframework.stereotype.Component;

@Component
public class PolitiqueEtTaxesPersistenceMapper {

    public PolitiqueEtTaxesEntity toEntity(PolitiqueEtTaxes domain) {
        if (domain == null) {
            return null;
        }
        return PolitiqueEtTaxesEntity.builder()
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

    public PolitiqueEtTaxes toDomain(PolitiqueEtTaxesEntity entity) {
        if (entity == null) {
            return null;
        }
        PolitiqueEtTaxes politiqueEtTaxes = new PolitiqueEtTaxes();
        politiqueEtTaxes.setIdPolitique(entity.getIdPolitique());
        politiqueEtTaxes.setGareRoutiereId(entity.getGareRoutiereId());
        politiqueEtTaxes.setNomPolitique(entity.getNomPolitique());
        politiqueEtTaxes.setDescription(entity.getDescription());
        politiqueEtTaxes.setTauxTaxe(entity.getTauxTaxe());
        politiqueEtTaxes.setMontantFixe(entity.getMontantFixe());
        politiqueEtTaxes.setDateEffet(entity.getDateEffet());
        politiqueEtTaxes.setDocumentUrl(entity.getDocumentUrl());
        politiqueEtTaxes.setType(entity.getType());
        return politiqueEtTaxes;
    }
}
