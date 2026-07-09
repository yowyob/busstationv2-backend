package cm.yowyob.bus_station_backend.application.service;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import cm.yowyob.bus_station_backend.application.dto.affiliation.AffiliationCreateDTO;
import cm.yowyob.bus_station_backend.application.dto.affiliation.AffiliationUpdateDTO;
import cm.yowyob.bus_station_backend.application.mapper.AffiliationMapper;
import cm.yowyob.bus_station_backend.application.port.out.AffiliationAgenceVoyagePersistencePort;
import cm.yowyob.bus_station_backend.application.port.out.AgencePersistencePort;
import cm.yowyob.bus_station_backend.application.port.out.GareRoutierePersistencePort;
import cm.yowyob.bus_station_backend.application.port.out.PolitiqueEtTaxesPort;
import cm.yowyob.bus_station_backend.domain.enums.PolitiqueOuTaxe;
import cm.yowyob.bus_station_backend.domain.enums.StatutTaxe;
import cm.yowyob.bus_station_backend.domain.exception.ResourceNotFoundException;
import cm.yowyob.bus_station_backend.domain.model.AffiliationAgenceVoyage;
import lombok.AllArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@AllArgsConstructor
public class AffiliationAgenceVoyageService {

    private final AffiliationAgenceVoyagePersistencePort affiliationAgenceVoyagePersistencePort;
    private final AgencePersistencePort agencePersistencePort;
    private final GareRoutierePersistencePort gareRoutierePersistencePort;
    private final PolitiqueEtTaxesPort politiqueEtTaxesPort;
    private final AffiliationMapper affiliationMapper;

    // -------------------- Lecture (existant + LOT 9) --------------------

    public Flux<AffiliationAgenceVoyage> getAffiliationsByGareRoutiereId(UUID gareRoutiereId) {
        return affiliationAgenceVoyagePersistencePort.findByGareRoutiereId(gareRoutiereId)
                .switchIfEmpty(Flux.empty());
    }

    public Mono<AffiliationAgenceVoyage> getById(UUID id) {
        return affiliationAgenceVoyagePersistencePort.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException(
                        "Contrat d'affiliation introuvable : " + id)));
    }

    public Flux<AffiliationAgenceVoyage> getAffiliationsByAgencyId(UUID agencyId) {
        return affiliationAgenceVoyagePersistencePort.findByAgencyId(agencyId)
                .switchIfEmpty(Flux.empty());
    }

    // -------------------- Création (LOT 9) --------------------

    /**
     * Création explicite via DTO. Vérifie l'existence agence + gare,
     * complète montant depuis taxes si non fourni, met statut EN_ATTENTE par défaut.
     */
    public Mono<AffiliationAgenceVoyage> createFromDTO(AffiliationCreateDTO dto) {
        return Mono.zip(
                agencePersistencePort.findById(dto.getAgencyId())
                        .switchIfEmpty(Mono.error(new ResourceNotFoundException(
                                "Agence introuvable : " + dto.getAgencyId()))),
                gareRoutierePersistencePort.getGareRoutiereById(dto.getGareRoutiereId())
                        .switchIfEmpty(Mono.error(new ResourceNotFoundException(
                                "Gare routière introuvable : " + dto.getGareRoutiereId()))))
                .flatMap(tuple -> {
                    String agencyName = (dto.getAgencyName() != null && !dto.getAgencyName().isBlank())
                            ? dto.getAgencyName()
                            : tuple.getT1().getLongName();

                    Mono<Double> montantMono = (dto.getMontantAffiliation() != null)
                            ? Mono.just(dto.getMontantAffiliation())
                            : computeMontantFromTaxesGare(dto.getGareRoutiereId());

                    LocalDate echeance = (dto.getEcheance() != null)
                            ? dto.getEcheance()
                            : LocalDate.now().plusYears(1);

                    return montantMono.flatMap(montant -> {
                        AffiliationAgenceVoyage affiliation = AffiliationAgenceVoyage.builder()
                                .agencyId(dto.getAgencyId())
                                .gareRoutiereId(dto.getGareRoutiereId())
                                .agencyName(agencyName)
                                .statut(StatutTaxe.EN_ATTENTE)
                                .echeance(echeance)
                                .montantAffiliation(montant)
                                .build();
                        return affiliationAgenceVoyagePersistencePort.save(affiliation);
                    });
                });
    }

    /**
     * Création legacy (utilisée à la création d'agence pour rétrocompatibilité).
     * Conserve la signature historique mais ne casse rien si aucune taxe n'est définie.
     */
    public Mono<Void> createAffiliation(UUID gareRoutiereId, UUID agencyId, String agencyName) {
        return Mono.zip(
                agencePersistencePort.findById(agencyId),
                gareRoutierePersistencePort.getGareRoutiereById(gareRoutiereId))
                .switchIfEmpty(Mono.error(new ResourceNotFoundException(
                        "L'agence ou la gare routière n'existe pas")))
                .flatMap(tuple -> computeMontantFromTaxesGare(gareRoutiereId)
                        .flatMap(montantTotal -> {
                            AffiliationAgenceVoyage affiliation = new AffiliationAgenceVoyage();
                            affiliation.setAgencyId(agencyId);
                            affiliation.setGareRoutiereId(gareRoutiereId);
                            affiliation.setStatut(StatutTaxe.EN_ATTENTE);
                            affiliation.setEcheance(LocalDate.now().plusYears(1));
                            affiliation.setMontantAffiliation(montantTotal);
                            affiliation.setAgencyName(agencyName);
                            return affiliationAgenceVoyagePersistencePort.save(affiliation);
                        }))
                .then();
    }

    // -------------------- Modification (LOT 9) --------------------

    public Mono<AffiliationAgenceVoyage> update(UUID id, AffiliationUpdateDTO dto) {
        return getById(id)
                .flatMap(existing -> {
                    affiliationMapper.applyUpdate(existing, dto);
                    return affiliationAgenceVoyagePersistencePort.save(existing);
                });
    }

    public Mono<AffiliationAgenceVoyage> updateStatut(UUID id, StatutTaxe statut) {
        if (statut == null) {
            return Mono.error(new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Le statut est obligatoire."));
        }
        return getById(id)
                .flatMap(existing -> {
                    existing.setStatut(statut);
                    return affiliationAgenceVoyagePersistencePort.save(existing);
                });
    }

    // -------------------- Helpers privés --------------------

    /**
     * Si aucune taxe n'est définie, retourne 0.0 plutôt qu'une erreur — pour ne pas
     * bloquer la création d'agence (cas où une gare n'a pas encore configuré ses taxes).
     */
    private Mono<Double> computeMontantFromTaxesGare(UUID gareRoutiereId) {
        return politiqueEtTaxesPort.findByGareRoutiereId(gareRoutiereId)
                .filter(politique -> politique.getType() == PolitiqueOuTaxe.TAXE)
                .collectList()
                .map(list -> list.stream()
                        .mapToDouble(p -> p.getMontantFixe() == null ? 0.0 : p.getMontantFixe())
                        .sum());
    }
}
