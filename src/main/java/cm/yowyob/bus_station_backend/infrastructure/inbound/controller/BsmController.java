package cm.yowyob.bus_station_backend.infrastructure.inbound.controller;

import cm.yowyob.bus_station_backend.application.dto.agence.AgenceVoyageResponseDTO;
import cm.yowyob.bus_station_backend.application.dto.agence.UpdateStatutAgenceDTO;
import cm.yowyob.bus_station_backend.application.dto.bsm.BsmProfilUpdateDTO;
import cm.yowyob.bus_station_backend.application.dto.bsm.BsmStatistiquesDTO;
import cm.yowyob.bus_station_backend.application.dto.politiquegare.PolitiqueGareCreateDTO;
import cm.yowyob.bus_station_backend.application.dto.politiquegare.PolitiqueGareResponseDTO;
import cm.yowyob.bus_station_backend.application.dto.taxe.TaxeAffiliationCreateDTO;
import cm.yowyob.bus_station_backend.application.dto.taxe.TaxeAffiliationResponseDTO;
import cm.yowyob.bus_station_backend.application.dto.taxe.TaxeAffiliationUpdateDTO;
import cm.yowyob.bus_station_backend.application.dto.user.UserDTO;
import cm.yowyob.bus_station_backend.application.dto.user.UserResponseDTO;
import cm.yowyob.bus_station_backend.application.port.in.AgenceUseCase;
import cm.yowyob.bus_station_backend.application.port.in.PolitiqueGareUseCase;
import cm.yowyob.bus_station_backend.application.port.in.TaxeAffiliationUseCase;
import cm.yowyob.bus_station_backend.application.port.in.UserUseCase;
import cm.yowyob.bus_station_backend.application.service.GareRoutiereService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static cm.yowyob.bus_station_backend.infrastructure.util.SecurityUtils.getCurrentUser;
import static cm.yowyob.bus_station_backend.infrastructure.util.SecurityUtils.getCurrentUserId;

/**
 * LOT 8 — Endpoints dédiés au Bus Station Manager (BSM).
 */
@RestController
@RequestMapping("/bsm")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "BSM", description = "Gestion du compte BSM et des opérations de gare")
@Slf4j
public class BsmController {

    private final UserUseCase userUseCase;
    private final GareRoutiereService gareRoutiereService;
    private final TaxeAffiliationUseCase taxeAffiliationUseCase;
    private final AgenceUseCase agenceUseCase;
    private final PolitiqueGareUseCase politiqueGareUseCase;

        @Operation(summary = "Profil du BSM connecté")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profil retourné",
                    content = @Content(schema = @Schema(implementation = UserResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Non authentifié"),
            @ApiResponse(responseCode = "403", description = "Pas BSM")
    })
    @GetMapping("/profil")
    @PreAuthorize("hasRole('BUS_STATION_MANAGER')")
    public Mono<ResponseEntity<UserResponseDTO>> getProfil() {
        return getCurrentUser()
                .map(UserResponseDTO::fromUser)
                .map(ResponseEntity::ok);
    }

    @Operation(summary = "Modifier son profil BSM")
    @PutMapping("/profil")
    @PreAuthorize("hasRole('BUS_STATION_MANAGER')")
    public Mono<ResponseEntity<UserResponseDTO>> updateProfil(@RequestBody BsmProfilUpdateDTO dto) {
        return getCurrentUserId()
                .flatMap(userId -> {
                    UserDTO userDTO = new UserDTO();
                    userDTO.setLast_name(dto.getNom());
                    userDTO.setFirst_name(dto.getPrenom());
                    userDTO.setEmail(dto.getEmail());
                    userDTO.setPhone_number(dto.getTelNumber());
                    return userUseCase.updateUserProfile(userId, userDTO);
                })
                .map(ResponseEntity::ok);
    }

        @Operation(summary = "Statistiques d'une gare pour le dashboard BSM")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "KPIs récupérés",
                    content = @Content(schema = @Schema(implementation = BsmStatistiquesDTO.class))),
            @ApiResponse(responseCode = "401", description = "Non authentifié"),
            @ApiResponse(responseCode = "403", description = "Pas BSM"),
            @ApiResponse(responseCode = "404", description = "Gare introuvable")
    })
    @GetMapping("/statistiques/{gareId}")
    @PreAuthorize("hasRole('BUS_STATION_MANAGER')")
    public Mono<ResponseEntity<BsmStatistiquesDTO>> getStatistiques(@PathVariable UUID gareId) {
        return gareRoutiereService.getStatistiques(gareId)
                .map(ResponseEntity::ok);
    }

    // --- TAXES ---
    @Operation(summary = "BSM : Créer une nouvelle taxe d'affiliation")
    @PostMapping("/taxe-affiliation")
    @PreAuthorize("hasRole('BUS_STATION_MANAGER')")
    public Mono<ResponseEntity<TaxeAffiliationResponseDTO>> createTaxe(@Valid @RequestBody TaxeAffiliationCreateDTO dto) {
        return taxeAffiliationUseCase.create(dto)
                .map(resp -> ResponseEntity.status(HttpStatus.CREATED).body(resp));
    }

    @Operation(summary = "BSM : Modifier une taxe existante")
    @PutMapping("/taxe-affiliation/{id}")
    @PreAuthorize("hasRole('BUS_STATION_MANAGER')")
    public Mono<ResponseEntity<TaxeAffiliationResponseDTO>> updateTaxe(@PathVariable UUID id, @RequestBody TaxeAffiliationUpdateDTO dto) {
        return taxeAffiliationUseCase.update(id, dto).map(ResponseEntity::ok);
    }

    // --- POLITIQUES ---
    @Operation(summary = "BSM : Ajouter une politique de gare")
    @PostMapping("/politique-gare")
    @PreAuthorize("hasRole('BUS_STATION_MANAGER')")
    public Mono<ResponseEntity<PolitiqueGareResponseDTO>> createPolitique(@Valid @RequestBody PolitiqueGareCreateDTO dto) {
        return politiqueGareUseCase.create(dto)
                .map(resp -> ResponseEntity.status(HttpStatus.CREATED).body(resp));
    }

    // --- AGENCES ---
    @Operation(summary = "BSM : Suspendre ou réactiver une agence")
    @PutMapping("/agence/{agenceId}/statut")
    @PreAuthorize("hasRole('BUS_STATION_MANAGER')")
    public Mono<ResponseEntity<AgenceVoyageResponseDTO>> updateAgenceStatut(
            @PathVariable UUID agenceId,
            @Valid @RequestBody UpdateStatutAgenceDTO dto) {
        return getCurrentUserId()
                .flatMap(userId -> agenceUseCase.updateStatutAgence(agenceId, dto.active(), dto.motif(), userId))
                .map(ResponseEntity::ok);
    }
}
