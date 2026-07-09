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

import cm.yowyob.bus_station_backend.application.dto.politiquegare.PolitiqueGareCreateDTO;
import cm.yowyob.bus_station_backend.application.dto.politiquegare.PolitiqueGareResponseDTO;
import cm.yowyob.bus_station_backend.application.dto.politiquegare.PolitiqueGareUpdateDTO;
import cm.yowyob.bus_station_backend.application.port.in.PolitiqueGareUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/politique-gare")
@AllArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class PolitiqueGareController {

    private final PolitiqueGareUseCase politiqueGareUseCase;

    @Operation(summary = "Lister les politiques d'une gare (public)")
    @GetMapping("/gare/{gareId}")
    // Lecture publique (configurée dans SecurityConfig en permitAll())
    public Flux<PolitiqueGareResponseDTO> getByGare(@PathVariable UUID gareId) {
        return politiqueGareUseCase.getByGareRoutiereId(gareId);
    }

    @Operation(summary = "Détail d'une politique de gare")
    @GetMapping("/{id}")
    public Mono<ResponseEntity<PolitiqueGareResponseDTO>> getById(@PathVariable UUID id) {
        return politiqueGareUseCase.getById(id).map(ResponseEntity::ok);
    }

    @Operation(summary = "Créer une politique de gare (BSM)")
    @PostMapping
    @PreAuthorize("hasRole('BUS_STATION_MANAGER')")
    public Mono<ResponseEntity<PolitiqueGareResponseDTO>> create(
            @Valid @RequestBody PolitiqueGareCreateDTO dto) {
        return politiqueGareUseCase.create(dto)
                .map(resp -> ResponseEntity.status(HttpStatus.CREATED).body(resp));
    }

    @Operation(summary = "Modifier une politique de gare (PATCH-like)")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('BUS_STATION_MANAGER')")
    public Mono<ResponseEntity<PolitiqueGareResponseDTO>> update(
            @PathVariable UUID id,
            @RequestBody PolitiqueGareUpdateDTO dto) {
        return politiqueGareUseCase.update(id, dto).map(ResponseEntity::ok);
    }

    @Operation(summary = "Supprimer une politique de gare")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('BUS_STATION_MANAGER')")
    public Mono<ResponseEntity<Void>> delete(@PathVariable UUID id) {
        return politiqueGareUseCase.delete(id)
                .thenReturn(ResponseEntity.noContent().<Void>build());
    }
}
