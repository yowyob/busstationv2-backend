package cm.yowyob.bus_station_backend.infrastructure.inbound.controller;

import cm.yowyob.bus_station_backend.application.dto.alerte.AlerteCreateDTO;
import cm.yowyob.bus_station_backend.application.dto.alerte.AlerteResponseDTO;
import cm.yowyob.bus_station_backend.application.mapper.AlerteMapper;
import cm.yowyob.bus_station_backend.application.port.in.AlerteUseCase;
import cm.yowyob.bus_station_backend.infrastructure.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/alerte")
@RequiredArgsConstructor
public class AlerteController {

    private final AlerteUseCase alerteUseCase;
    private final AlerteMapper  alerteMapper;

    // -------------------------------------------------------
    // POST /alerte
    // BSM envoie une alerte à une agence
    // Le gareId est déduit du BSM connecté via gare_routiere.manager_id
    // -------------------------------------------------------
    @PostMapping
    @PreAuthorize("hasRole('BUS_STATION_MANAGER')")
    public Mono<ResponseEntity<AlerteResponseDTO>> createAlerte(
            @Valid @RequestBody AlerteCreateDTO dto,
            @RequestParam UUID gareId) {

        return SecurityUtils.getCurrentUserId()
                .flatMap(bsmId -> alerteUseCase.createAlerte(dto, gareId, bsmId))
                .map(alerte -> ResponseEntity
                        .status(HttpStatus.CREATED)
                        .body(alerteMapper.toResponse(alerte)));
    }

    // -------------------------------------------------------
    // GET /alerte/gare/{gareId}
    // Historique des alertes envoyées par une gare
    // -------------------------------------------------------
    @GetMapping("/gare/{gareId}")
    @PreAuthorize("hasRole('BUS_STATION_MANAGER')")
    public Flux<AlerteResponseDTO> getAlertesByGare(@PathVariable UUID gareId) {
        return alerteUseCase.getAlertesByGare(gareId)
                .map(alerteMapper::toResponse);
    }

    // -------------------------------------------------------
    // GET /alerte/agence/{agenceId}
    // Alertes reçues par une agence
    // -------------------------------------------------------
    @GetMapping("/agence/{agenceId}")
    @PreAuthorize("hasRole('AGENCE_VOYAGE')")
    public Flux<AlerteResponseDTO> getAlertesByAgence(@PathVariable UUID agenceId) {
        return alerteUseCase.getAlertesByAgence(agenceId)
                .map(alerteMapper::toResponse);
    }

    // -------------------------------------------------------
    // PUT /alerte/{id}/lu
    // Marquer une alerte comme lue
    // -------------------------------------------------------
    @PutMapping("/{id}/lu")
    @PreAuthorize("hasRole('AGENCE_VOYAGE')")
    public Mono<ResponseEntity<AlerteResponseDTO>> marquerLu(@PathVariable UUID id) {
        return alerteUseCase.marquerLu(id)
                .map(alerte -> ResponseEntity.ok(alerteMapper.toResponse(alerte)));
    }
}