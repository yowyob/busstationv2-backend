package cm.yowyob.bus_station_backend.infrastructure.inbound.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cm.yowyob.bus_station_backend.application.dto.taxe.TaxeAffiliationAgenceResponseDTO;
import cm.yowyob.bus_station_backend.application.dto.taxe.TaxeAffiliationCreateDTO;
import cm.yowyob.bus_station_backend.application.dto.taxe.TaxeAffiliationResponseDTO;
import cm.yowyob.bus_station_backend.application.dto.taxe.TaxeAffiliationUpdateDTO;
import cm.yowyob.bus_station_backend.application.port.in.TaxeAffiliationUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/taxe-affiliation")
@AllArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class TaxeAffiliationController {

    private final TaxeAffiliationUseCase taxeAffiliationUseCase;

    @Operation(summary = "Lister toutes les taxes d'une gare")
    @GetMapping("/gare/{gareId}")
    @PreAuthorize("hasRole('BUS_STATION_MANAGER') or hasRole('AGENCE_VOYAGE')")
    public Flux<TaxeAffiliationResponseDTO> getByGare(@PathVariable UUID gareId) {
        return taxeAffiliationUseCase.getByGareRoutiereId(gareId);
    }

    @Operation(summary = "Taxes dues par une agence (calculé via la gare à laquelle elle est rattachée)")
    @GetMapping("/agence/{agenceId}")
    @PreAuthorize("hasRole('BUS_STATION_MANAGER') or hasRole('AGENCE_VOYAGE')")
    public Mono<ResponseEntity<TaxeAffiliationAgenceResponseDTO>> getByAgence(@PathVariable UUID agenceId) {
        return taxeAffiliationUseCase.getByAgence(agenceId)
                .map(ResponseEntity::ok);
    }

    @Operation(summary = "Détail d'une taxe")
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('BUS_STATION_MANAGER') or hasRole('AGENCE_VOYAGE')")
    public Mono<ResponseEntity<TaxeAffiliationResponseDTO>> getById(@PathVariable UUID id) {
        return taxeAffiliationUseCase.getById(id).map(ResponseEntity::ok);
    }

    @Operation(summary = "Créer une taxe (BSM)")
    @PostMapping
    @PreAuthorize("hasRole('BUS_STATION_MANAGER')")
    public Mono<ResponseEntity<TaxeAffiliationResponseDTO>> create(
            @Valid @RequestBody TaxeAffiliationCreateDTO dto) {
        return taxeAffiliationUseCase.create(dto)
                .map(resp -> ResponseEntity.status(HttpStatus.CREATED).body(resp));
    }

    @Operation(summary = "Modifier une taxe (PATCH-like)")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('BUS_STATION_MANAGER')")
    public Mono<ResponseEntity<TaxeAffiliationResponseDTO>> update(
            @PathVariable UUID id,
            @RequestBody TaxeAffiliationUpdateDTO dto) {
        return taxeAffiliationUseCase.update(id, dto).map(ResponseEntity::ok);
    }

    @Operation(summary = "Supprimer une taxe")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('BUS_STATION_MANAGER')")
    public Mono<ResponseEntity<Void>> delete(@PathVariable UUID id) {
        return taxeAffiliationUseCase.delete(id)
                .thenReturn(ResponseEntity.noContent().<Void>build());
    }
}
