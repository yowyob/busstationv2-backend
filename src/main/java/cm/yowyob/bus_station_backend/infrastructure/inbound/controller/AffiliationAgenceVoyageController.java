package cm.yowyob.bus_station_backend.infrastructure.inbound.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cm.yowyob.bus_station_backend.application.dto.affiliation.AffiliationCreateDTO;
import cm.yowyob.bus_station_backend.application.dto.affiliation.AffiliationResponseDTO;
import cm.yowyob.bus_station_backend.application.dto.affiliation.AffiliationStatutDTO;
import cm.yowyob.bus_station_backend.application.dto.affiliation.AffiliationUpdateDTO;
import cm.yowyob.bus_station_backend.application.mapper.AffiliationMapper;
import cm.yowyob.bus_station_backend.application.service.AffiliationAgenceVoyageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/affiliation")
@AllArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class AffiliationAgenceVoyageController {

    private final AffiliationAgenceVoyageService affiliationAgenceVoyageService;
    private final AffiliationMapper affiliationMapper;

    // -------------------- Lecture --------------------

    @Operation(summary = "Récupérer les affiliations d'une gare routière")
    @GetMapping("/gare/{gareId}")
    @PreAuthorize("hasRole('BUS_STATION_MANAGER')")
    public Flux<AffiliationResponseDTO> getAffiliationsByGareRoutiereId(@PathVariable UUID gareId) {
        return affiliationAgenceVoyageService.getAffiliationsByGareRoutiereId(gareId)
                .map(affiliationMapper::toResponseDTO);
    }

    @Operation(summary = "Détail d'un contrat d'affiliation")
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('BUS_STATION_MANAGER') or hasRole('AGENCE_VOYAGE')")
    public Mono<ResponseEntity<AffiliationResponseDTO>> getById(@PathVariable UUID id) {
        return affiliationAgenceVoyageService.getById(id)
                .map(affiliationMapper::toResponseDTO)
                .map(ResponseEntity::ok);
    }

    @Operation(summary = "Liste des affiliations d'une agence (toutes gares confondues)")
    @GetMapping("/agence/{agencyId}")
    @PreAuthorize("hasRole('BUS_STATION_MANAGER') or hasRole('AGENCE_VOYAGE')")
    public Flux<AffiliationResponseDTO> getByAgencyId(@PathVariable UUID agencyId) {
        return affiliationAgenceVoyageService.getAffiliationsByAgencyId(agencyId)
                .map(affiliationMapper::toResponseDTO);
    }

    // -------------------- Création --------------------

    @Operation(summary = "Créer un contrat d'affiliation Gare ↔ Agence (BSM)")
    @PostMapping
    @PreAuthorize("hasRole('BUS_STATION_MANAGER')")
    public Mono<ResponseEntity<AffiliationResponseDTO>> create(
            @Valid @RequestBody AffiliationCreateDTO dto) {
        return affiliationAgenceVoyageService.createFromDTO(dto)
                .map(affiliationMapper::toResponseDTO)
                .map(resp -> ResponseEntity.status(HttpStatus.CREATED).body(resp));
    }

    // -------------------- Modification --------------------

    @Operation(summary = "Modifier un contrat d'affiliation (PATCH-like, champs null ignorés)")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('BUS_STATION_MANAGER')")
    public Mono<ResponseEntity<AffiliationResponseDTO>> update(
            @PathVariable UUID id,
            @RequestBody AffiliationUpdateDTO dto) {
        return affiliationAgenceVoyageService.update(id, dto)
                .map(affiliationMapper::toResponseDTO)
                .map(ResponseEntity::ok);
    }

    @Operation(summary = "Changer le statut d'un contrat (EN_ATTENTE / PAYE / EN_RETARD)")
    @PutMapping("/{id}/statut")
    @PreAuthorize("hasRole('BUS_STATION_MANAGER')")
    public Mono<ResponseEntity<AffiliationResponseDTO>> updateStatut(
            @PathVariable UUID id,
            @Valid @RequestBody AffiliationStatutDTO dto) {
        return affiliationAgenceVoyageService.updateStatut(id, dto.getStatut())
                .map(affiliationMapper::toResponseDTO)
                .map(ResponseEntity::ok);
    }
}
