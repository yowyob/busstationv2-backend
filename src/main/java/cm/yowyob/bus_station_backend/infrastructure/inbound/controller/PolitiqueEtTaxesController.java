package cm.yowyob.bus_station_backend.infrastructure.inbound.controller;

import cm.yowyob.bus_station_backend.application.dto.politique.PolitiqueEtTaxesDTO;
import cm.yowyob.bus_station_backend.application.dto.politique.PolitiqueEtTaxesRequestDTO;
import cm.yowyob.bus_station_backend.application.mapper.PolitiqueEtTaxesMapper;
import cm.yowyob.bus_station_backend.application.port.in.PolitiqueEtTaxesUseCase;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/politique-et-taxes")
@AllArgsConstructor
public class PolitiqueEtTaxesController {

    private final PolitiqueEtTaxesUseCase politiqueEtTaxesUseCase;
    private final PolitiqueEtTaxesMapper politiqueEtTaxesMapper;

    @PostMapping(value = "/add", consumes = { MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE })
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<PolitiqueEtTaxesDTO> createPolitique(
            @RequestPart("politique") PolitiqueEtTaxesRequestDTO requestDTO,
            @RequestPart(value = "file", required = false) Mono<FilePart> filePartMono) {
        return politiqueEtTaxesUseCase.createPolitique(politiqueEtTaxesMapper.fromRequestDTO(requestDTO), filePartMono)
                .map(politiqueEtTaxesMapper::toDTO);
    }

    @PutMapping(value = "/{id}", consumes = { MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE })
    public Mono<PolitiqueEtTaxesDTO> updatePolitique(
            @PathVariable UUID id,
            @RequestPart("politique") PolitiqueEtTaxesRequestDTO requestDTO,
            @RequestPart(value = "file", required = false) Mono<FilePart> filePartMono) {
        return politiqueEtTaxesUseCase
                .updatePolitique(id, politiqueEtTaxesMapper.fromRequestDTO(requestDTO), filePartMono)
                .map(politiqueEtTaxesMapper::toDTO);
    }

    @GetMapping("/{id}")
    public Mono<PolitiqueEtTaxesDTO> getPolitiqueById(@PathVariable UUID id) {
        return politiqueEtTaxesUseCase.getById(id)
                .map(politiqueEtTaxesMapper::toDTO);
    }

    @GetMapping("/gare-routiere/{gareRoutiereId}")
    public Flux<PolitiqueEtTaxesDTO> getAllPolitiquesByGareRoutiere(@PathVariable UUID gareRoutiereId) {
        return politiqueEtTaxesUseCase.getAllByGareRoutiere(gareRoutiereId)
                .map(politiqueEtTaxesMapper::toDTO);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deletePolitique(@PathVariable UUID id) {
        return politiqueEtTaxesUseCase.deletePolitique(id);
    }
}
