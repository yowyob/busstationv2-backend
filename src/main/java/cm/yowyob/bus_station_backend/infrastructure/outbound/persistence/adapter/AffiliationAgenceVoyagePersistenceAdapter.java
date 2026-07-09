package cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.adapter;

import java.time.LocalDateTime;
import java.util.UUID;

import cm.yowyob.bus_station_backend.application.port.out.AffiliationAgenceVoyagePersistencePort;
import cm.yowyob.bus_station_backend.domain.model.AffiliationAgenceVoyage;
import cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.entity.AffiliationAgenceVoyageEntity;
import cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.repository.AffiliationAgenceVoyageR2dbcRepository;
import org.springframework.stereotype.Component;
import lombok.AllArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@AllArgsConstructor
public class AffiliationAgenceVoyagePersistenceAdapter implements AffiliationAgenceVoyagePersistencePort {

    private final AffiliationAgenceVoyageR2dbcRepository affiliationAgenceVoyageR2dbcRepository;

    @Override
    public Mono<AffiliationAgenceVoyage> save(AffiliationAgenceVoyage affiliationAgenceVoyage) {
        AffiliationAgenceVoyageEntity entity = mapToEntity(affiliationAgenceVoyage);

        if (entity.getId() == null) {
            // INSERT pur
            entity.setId(UUID.randomUUID());
            entity.setCreatedAt(LocalDateTime.now());
            entity.setUpdatedAt(LocalDateTime.now());
            entity.setAsNew();
            return affiliationAgenceVoyageR2dbcRepository.save(entity)
                    .map(this::mapToDomain);
        }

        // ID fourni : on doit déterminer si c'est INSERT ou UPDATE
        return affiliationAgenceVoyageR2dbcRepository.existsById(entity.getId())
                .flatMap(exists -> {
                    if (Boolean.TRUE.equals(exists)) {
                        entity.setIsNew(false);
                        entity.setUpdatedAt(LocalDateTime.now());
                        // Conserver createdAt si non fourni
                        if (entity.getCreatedAt() == null) {
                            entity.setCreatedAt(LocalDateTime.now());
                        }
                        // isNew=false par défaut → UPDATE
                        return affiliationAgenceVoyageR2dbcRepository.save(entity);
                    } else {
                        // ID fourni mais inexistant : INSERT
                        if (entity.getCreatedAt() == null) {
                            entity.setCreatedAt(LocalDateTime.now());
                        }
                        entity.setUpdatedAt(LocalDateTime.now());
                        entity.setAsNew();
                        return affiliationAgenceVoyageR2dbcRepository.save(entity);
                    }
                })
                .map(this::mapToDomain);
    }

    @Override
    public Flux<AffiliationAgenceVoyage> findByGareRoutiereId(UUID gareRoutiereId) {
        return affiliationAgenceVoyageR2dbcRepository.findByGareRoutiereId(gareRoutiereId)
                .map(this::mapToDomain);
    }

    @Override
    public Mono<AffiliationAgenceVoyage> findById(UUID id) {
        return affiliationAgenceVoyageR2dbcRepository.findById(id)
                .map(this::mapToDomain);
    }

    @Override
    public Flux<AffiliationAgenceVoyage> findByAgencyId(UUID agencyId) {
        return affiliationAgenceVoyageR2dbcRepository.findByAgencyId(agencyId)
                .map(this::mapToDomain);
    }

    @Override
    public Mono<Void> deleteById(UUID id) {
        return affiliationAgenceVoyageR2dbcRepository.deleteById(id);
    }

    // --- helpers ---

    private AffiliationAgenceVoyageEntity mapToEntity(AffiliationAgenceVoyage domain) {
        if (domain == null) {
            return null;
        }
        return AffiliationAgenceVoyageEntity.builder()
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

    private AffiliationAgenceVoyage mapToDomain(AffiliationAgenceVoyageEntity entity) {
        if (entity == null) {
            return null;
        }
        return AffiliationAgenceVoyage.builder()
                .id(entity.getId())
                .gareRoutiereId(entity.getGareRoutiereId())
                .agencyId(entity.getAgencyId())
                .agencyName(entity.getAgencyName())
                .statut(entity.getStatut())
                .echeance(entity.getEcheance())
                .montantAffiliation(entity.getMontantAffiliation())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
