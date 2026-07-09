// src/main/java/cm/yowyob/bus_station_backend/infrastructure/inbound/controller/GareRoutiereController.java

package cm.yowyob.bus_station_backend.infrastructure.inbound.controller;

import cm.yowyob.bus_station_backend.application.dto.agence.AgenceVoyageResponseDTO;
import cm.yowyob.bus_station_backend.application.dto.gareRoutiere.GareRoutiereDTO;
import cm.yowyob.bus_station_backend.application.dto.gareRoutiere.GareRoutierePreviewDTO;
import cm.yowyob.bus_station_backend.application.dto.gareRoutiere.GareRoutiereRequestDTO;
import cm.yowyob.bus_station_backend.application.dto.gareRoutiere.GareRoutiereUpdateDTO;
import cm.yowyob.bus_station_backend.application.dto.voyage.VoyagePreviewDTO;
import cm.yowyob.bus_station_backend.application.mapper.AgenceVoyageMapper;
import cm.yowyob.bus_station_backend.application.mapper.GareRoutiereMapper;
import cm.yowyob.bus_station_backend.application.mapper.VoyageMapper;
import cm.yowyob.bus_station_backend.application.service.GareRoutiereService;
import cm.yowyob.bus_station_backend.domain.enums.ServicesGareRoutiere;
import cm.yowyob.bus_station_backend.domain.model.GareRoutiere;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static cm.yowyob.bus_station_backend.infrastructure.util.SecurityUtils.getCurrentUserId;

@RestController
@RequiredArgsConstructor
@RequestMapping("/gare")
public class GareRoutiereController {

    private final GareRoutiereService gareRoutiereService;
    private final GareRoutiereMapper gareRoutiereMapper;
    private final AgenceVoyageMapper agenceVoyageMapper;
    private final VoyageMapper voyageMapper;

    // ============================================================
    // EXISTANT
    // ============================================================

    @Operation(summary = "Créer une nouvelle gare routière")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Gare créée",
                    content = @Content(schema = @Schema(implementation = GareRoutiereDTO.class))),
            @ApiResponse(responseCode = "400", description = "Requête invalide"),
            @ApiResponse(responseCode = "401", description = "Non autorisé"),
            @ApiResponse(responseCode = "403", description = "Accès refusé"),
            @ApiResponse(responseCode = "404", description = "Gestionnaire non trouvé")
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    public Mono<ResponseEntity<GareRoutiere>> createGareRoutiere(
            @RequestBody @Valid GareRoutiereRequestDTO requestDTO) {
        return gareRoutiereService.createGareRoutiere(gareRoutiereMapper.toDomain(requestDTO))
                .map(gareRoutiere -> ResponseEntity
                        .created(URI.create("/gare/" + gareRoutiere.getIdGareRoutiere()))
                        .body(gareRoutiere));
    }

    @Operation(summary = "Rechercher des gares routières")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Liste récupérée",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = GareRoutierePreviewDTO.class))))
    })
    @GetMapping
    public Mono<Page<GareRoutierePreviewDTO>> getAllGaresRoutieres(
            @RequestParam(required = false) String searchTerm,
            @RequestParam(required = false) List<ServicesGareRoutiere> services,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return gareRoutiereService.getAllGaresRoutieres(searchTerm, services, PageRequest.of(page, size))
                .map(p -> p.map(gareRoutiereMapper::toPreviewDTO));
    }

    @Operation(summary = "Détails d'une gare routière par ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = GareRoutiereDTO.class))),
            @ApiResponse(responseCode = "404", description = "Gare non trouvée")
    })
    @GetMapping("/{gareId}")
    public Mono<ResponseEntity<GareRoutiereDTO>> getGareRoutiereById(@PathVariable UUID gareId) {
        return gareRoutiereService.getGareRoutiereById(gareId)
                .map(ResponseEntity::ok);
    }

    @Operation(summary = "Détails d'une gare routière par ID du manager")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "401", description = "Non autorisé"),
            @ApiResponse(responseCode = "404", description = "Gare non trouvée")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('BUS_STATION_MANAGER')")
    @GetMapping("/manager/{managerId}")
    public Mono<ResponseEntity<GareRoutiereDTO>> getGareRoutiereByManagerId(@PathVariable UUID managerId) {
        return gareRoutiereService.getGareRoutiereByManagerId(managerId)
                .map(ResponseEntity::ok);
    }

    // ============================================================
    // LOT 8 — Gare & BSM
    // ============================================================

    @Operation(summary = "[LOT 8] Modifier une gare (BSM)",
            description = "Le BSM connecté ne peut modifier que sa propre gare.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Gare modifiée",
                    content = @Content(schema = @Schema(implementation = GareRoutiereDTO.class))),
            @ApiResponse(responseCode = "400", description = "Requête invalide"),
            @ApiResponse(responseCode = "401", description = "Non autorisé"),
            @ApiResponse(responseCode = "403", description = "Pas le manager de cette gare"),
            @ApiResponse(responseCode = "404", description = "Gare non trouvée")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('BUS_STATION_MANAGER')")
    @PutMapping("/{gareId}")
    public Mono<ResponseEntity<GareRoutiereDTO>> updateGareRoutiere(
            @PathVariable UUID gareId,
            @RequestBody GareRoutiereUpdateDTO dto) {
        return getCurrentUserId()
                .flatMap(userId -> gareRoutiereService.updateGareRoutiere(gareId, dto, userId))
                .map(ResponseEntity::ok);
    }

    @Operation(summary = "[LOT 8] Liste des agences affiliées à une gare")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "404", description = "Gare non trouvée")
    })
    @GetMapping("/{gareId}/agences")
    public Flux<AgenceVoyageResponseDTO> getAgencesByGareId(@PathVariable UUID gareId) {
        return gareRoutiereService.getAgencesByGareId(gareId)
                .map(agenceVoyageMapper::toResponseDTO);
    }

    @Operation(summary = "[LOT 8] Voyages d'une gare (filtrable par date)",
            description = "Si `date` n'est pas fournie, retourne tous les voyages de la gare. Format: YYYY-MM-DD.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "404", description = "Gare non trouvée")
    })
    @GetMapping("/{gareId}/voyages")
    public Mono<Page<VoyagePreviewDTO>> getVoyagesByGareId(
            @PathVariable UUID gareId,
            @Parameter(description = "Date au format YYYY-MM-DD")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return gareRoutiereService.getVoyagesByGareId(gareId, date, PageRequest.of(page, size))
                .map(p -> p.map(voyageMapper::toPreviewDTO));
    }
}