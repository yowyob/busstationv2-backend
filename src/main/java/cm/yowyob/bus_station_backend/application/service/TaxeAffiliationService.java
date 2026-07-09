package cm.yowyob.bus_station_backend.application.service;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import cm.yowyob.bus_station_backend.application.dto.taxe.TaxeAffiliationAgenceResponseDTO;
import cm.yowyob.bus_station_backend.application.dto.taxe.TaxeAffiliationCreateDTO;
import cm.yowyob.bus_station_backend.application.dto.taxe.TaxeAffiliationResponseDTO;
import cm.yowyob.bus_station_backend.application.dto.taxe.TaxeAffiliationUpdateDTO;
import cm.yowyob.bus_station_backend.application.mapper.TaxeAffiliationMapper;
import cm.yowyob.bus_station_backend.application.port.in.TaxeAffiliationUseCase;
import cm.yowyob.bus_station_backend.application.port.out.AgencePersistencePort;
import cm.yowyob.bus_station_backend.application.port.out.PolitiqueEtTaxesPort;
import cm.yowyob.bus_station_backend.domain.enums.PolitiqueOuTaxe;
import cm.yowyob.bus_station_backend.domain.exception.ResourceNotFoundException;
import cm.yowyob.bus_station_backend.domain.model.PolitiqueEtTaxes;
import lombok.AllArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@AllArgsConstructor
public class TaxeAffiliationService implements TaxeAffiliationUseCase {

    private final PolitiqueEtTaxesPort politiqueEtTaxesPort;
    private final AgencePersistencePort agencePersistencePort;
    private final TaxeAffiliationMapper taxeAffiliationMapper;

    @Override
    public Flux<TaxeAffiliationResponseDTO> getByGareRoutiereId(UUID gareRoutiereId) {
        return politiqueEtTaxesPort.findByGareRoutiereIdAndType(gareRoutiereId, PolitiqueOuTaxe.TAXE)
                .map(taxeAffiliationMapper::toResponseDTO);
    }

    @Override
    public Mono<TaxeAffiliationAgenceResponseDTO> getByAgence(UUID agencyId) {
        return agencePersistencePort.findById(agencyId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException(
                        "Agence introuvable : " + agencyId)))
                .flatMap(agence -> {
                    UUID gareId = agence.getGareRoutiereId();
                    if (gareId == null) {
                        // Agence non rattachée à une gare : retour vide cohérent
                        return Mono.just(TaxeAffiliationAgenceResponseDTO.builder()
                                .agencyId(agencyId)
                                .gareRoutiereId(null)
                                .montantTotalDu(0.0)
                                .taxes(java.util.List.of())
                                .build());
                    }
                    return politiqueEtTaxesPort.findByGareRoutiereIdAndType(gareId, PolitiqueOuTaxe.TAXE)
                            .map(taxeAffiliationMapper::toResponseDTO)
                            .collectList()
                            .map(list -> {
                                double total = list.stream()
                                        .mapToDouble(t -> t.getMontantFixe() == null ? 0.0 : t.getMontantFixe())
                                        .sum();
                                return TaxeAffiliationAgenceResponseDTO.builder()
                                        .agencyId(agencyId)
                                        .gareRoutiereId(gareId)
                                        .montantTotalDu(total)
                                        .taxes(list)
                                        .build();
                            });
                });
    }

    @Override
    public Mono<TaxeAffiliationResponseDTO> getById(UUID id) {
        return politiqueEtTaxesPort.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException(
                        "Taxe introuvable : " + id)))
                .flatMap(p -> {
                    if (p.getType() != PolitiqueOuTaxe.TAXE) {
                        return Mono.error(new ResourceNotFoundException(
                                "L'élément " + id + " n'est pas une taxe (type=" + p.getType() + ")"));
                    }
                    return Mono.just(taxeAffiliationMapper.toResponseDTO(p));
                });
    }

    @Override
    public Mono<TaxeAffiliationResponseDTO> create(TaxeAffiliationCreateDTO dto) {
        // Validation : au moins l'un des deux montants doit être renseigné
        if ((dto.getMontantFixe() == null || dto.getMontantFixe() <= 0)
                && (dto.getTauxTaxe() == null || dto.getTauxTaxe() <= 0)) {
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Au moins l'un des champs montantFixe ou tauxTaxe doit être strictement positif."));
        }
        PolitiqueEtTaxes domain = taxeAffiliationMapper.fromCreateDTO(dto);
        return politiqueEtTaxesPort.save(domain)
                .map(taxeAffiliationMapper::toResponseDTO);
    }

    @Override
    public Mono<TaxeAffiliationResponseDTO> update(UUID id, TaxeAffiliationUpdateDTO dto) {
        return politiqueEtTaxesPort.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException(
                        "Taxe introuvable : " + id)))
                .flatMap(existing -> {
                    if (existing.getType() != PolitiqueOuTaxe.TAXE) {
                        return Mono.error(new ResourceNotFoundException(
                                "L'élément " + id + " n'est pas une taxe."));
                    }
                    taxeAffiliationMapper.applyUpdate(existing, dto);
                    return politiqueEtTaxesPort.save(existing);
                })
                .map(taxeAffiliationMapper::toResponseDTO);
    }

    @Override
    public Mono<Void> delete(UUID id) {
        return politiqueEtTaxesPort.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException(
                        "Taxe introuvable : " + id)))
                .flatMap(existing -> {
                    if (existing.getType() != PolitiqueOuTaxe.TAXE) {
                        return Mono.error(new ResourceNotFoundException(
                                "L'élément " + id + " n'est pas une taxe."));
                    }
                    return politiqueEtTaxesPort.deleteById(id);
                });
    }
}
