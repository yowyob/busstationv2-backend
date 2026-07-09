package cm.yowyob.bus_station_backend.application.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import cm.yowyob.bus_station_backend.application.dto.politiquegare.PolitiqueGareCreateDTO;
import cm.yowyob.bus_station_backend.application.dto.politiquegare.PolitiqueGareResponseDTO;
import cm.yowyob.bus_station_backend.application.dto.politiquegare.PolitiqueGareUpdateDTO;
import cm.yowyob.bus_station_backend.application.mapper.PolitiqueGareMapper;
import cm.yowyob.bus_station_backend.application.port.in.PolitiqueGareUseCase;
import cm.yowyob.bus_station_backend.application.port.out.PolitiqueEtTaxesPort;
import cm.yowyob.bus_station_backend.domain.enums.PolitiqueOuTaxe;
import cm.yowyob.bus_station_backend.domain.exception.ResourceNotFoundException;
import cm.yowyob.bus_station_backend.domain.model.PolitiqueEtTaxes;
import lombok.AllArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@AllArgsConstructor
public class PolitiqueGareService implements PolitiqueGareUseCase {

    private final PolitiqueEtTaxesPort politiqueEtTaxesPort;
    private final PolitiqueGareMapper politiqueGareMapper;

    @Override
    public Flux<PolitiqueGareResponseDTO> getByGareRoutiereId(UUID gareRoutiereId) {
        return politiqueEtTaxesPort.findByGareRoutiereIdAndType(gareRoutiereId, PolitiqueOuTaxe.POLITIQUE)
                .map(politiqueGareMapper::toResponseDTO);
    }

    @Override
    public Mono<PolitiqueGareResponseDTO> getById(UUID id) {
        return politiqueEtTaxesPort.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException(
                        "Politique introuvable : " + id)))
                .flatMap(p -> {
                    if (p.getType() != PolitiqueOuTaxe.POLITIQUE) {
                        return Mono.error(new ResourceNotFoundException(
                                "L'élément " + id + " n'est pas une politique de gare (type=" + p.getType() + ")"));
                    }
                    return Mono.just(politiqueGareMapper.toResponseDTO(p));
                });
    }

    @Override
    public Mono<PolitiqueGareResponseDTO> create(PolitiqueGareCreateDTO dto) {
        PolitiqueEtTaxes domain = politiqueGareMapper.fromCreateDTO(dto);
        return politiqueEtTaxesPort.save(domain)
                .map(politiqueGareMapper::toResponseDTO);
    }

    @Override
    public Mono<PolitiqueGareResponseDTO> update(UUID id, PolitiqueGareUpdateDTO dto) {
        return politiqueEtTaxesPort.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException(
                        "Politique introuvable : " + id)))
                .flatMap(existing -> {
                    if (existing.getType() != PolitiqueOuTaxe.POLITIQUE) {
                        return Mono.error(new ResourceNotFoundException(
                                "L'élément " + id + " n'est pas une politique de gare."));
                    }
                    politiqueGareMapper.applyUpdate(existing, dto);
                    return politiqueEtTaxesPort.save(existing);
                })
                .map(politiqueGareMapper::toResponseDTO);
    }

    @Override
    public Mono<Void> delete(UUID id) {
        return politiqueEtTaxesPort.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException(
                        "Politique introuvable : " + id)))
                .flatMap(existing -> {
                    if (existing.getType() != PolitiqueOuTaxe.POLITIQUE) {
                        return Mono.error(new ResourceNotFoundException(
                                "L'élément " + id + " n'est pas une politique de gare."));
                    }
                    return politiqueEtTaxesPort.deleteById(id);
                });
    }
}
