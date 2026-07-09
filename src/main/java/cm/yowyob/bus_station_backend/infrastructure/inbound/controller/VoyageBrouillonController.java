package cm.yowyob.bus_station_backend.infrastructure.inbound.controller;

import cm.yowyob.bus_station_backend.application.dto.voyage.VoyageBrouillonCreateDTO;
import cm.yowyob.bus_station_backend.application.dto.voyage.VoyageBrouillonResponseDTO;
import cm.yowyob.bus_station_backend.application.dto.voyage.VoyageBrouillonUpdateDTO;
import cm.yowyob.bus_station_backend.application.dto.voyage.VoyageDetailsDTO;
import cm.yowyob.bus_station_backend.application.port.in.VoyageBrouillonUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import static cm.yowyob.bus_station_backend.infrastructure.util.SecurityUtils.getCurrentUserId;

import java.util.UUID;

@RestController
@RequestMapping("/voyage/brouillon")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class VoyageBrouillonController {

    private final VoyageBrouillonUseCase brouillonUseCase;

    @Operation(summary = "Créer un brouillon de voyage")
    @PostMapping
    public Mono<ResponseEntity<VoyageBrouillonResponseDTO>> create(
            @Valid @RequestBody VoyageBrouillonCreateDTO dto) {
        return brouillonUseCase.create(dto)
                .map(created -> new ResponseEntity<>(created, HttpStatus.CREATED));
    }

    @Operation(summary = "Détail d'un brouillon")
    @GetMapping("/{id}")
    public Mono<ResponseEntity<VoyageBrouillonResponseDTO>> getById(@PathVariable UUID id) {
        return brouillonUseCase.getById(id).map(ResponseEntity::ok);
    }

    @Operation(summary = "Modifier un brouillon (PATCH-like, champs null ignorés)")
    @PutMapping("/{id}")
    public Mono<ResponseEntity<VoyageBrouillonResponseDTO>> update(
            @PathVariable UUID id,
            @RequestBody VoyageBrouillonUpdateDTO dto) {
        return brouillonUseCase.update(id, dto).map(ResponseEntity::ok);
    }

    @Operation(summary = "Supprimer un brouillon")
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> delete(@PathVariable UUID id) {
        return brouillonUseCase.delete(id)
                .thenReturn(ResponseEntity.noContent().<Void>build());
    }

    @Operation(summary = "Publier un brouillon : crée un Voyage publié et marque le brouillon CONVERTI")
    @PostMapping("/{id}/publier")
    public Mono<ResponseEntity<VoyageDetailsDTO>> publish(@PathVariable UUID id) {
        return getCurrentUserId()
                .flatMap(userId -> brouillonUseCase.publish(id, userId))
                .map(ResponseEntity::ok);
    }
}