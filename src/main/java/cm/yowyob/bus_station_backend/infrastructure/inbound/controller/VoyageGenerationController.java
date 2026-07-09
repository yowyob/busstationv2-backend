package cm.yowyob.bus_station_backend.infrastructure.inbound.controller;

import cm.yowyob.bus_station_backend.application.dto.voyage.generation.*;
import cm.yowyob.bus_station_backend.application.port.in.VoyageGenerationUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import static cm.yowyob.bus_station_backend.infrastructure.util.SecurityUtils.getCurrentUserId;

@RestController
@RequiredArgsConstructor
@RequestMapping("/voyage")
@SecurityRequirement(name = "bearerAuth")
@Slf4j
public class VoyageGenerationController {

    private final VoyageGenerationUseCase generationUseCase;

    @Operation(summary = "Générer un voyage à partir d'une ligne de service et d'une date",
            description = "Si publierDirectement=true ET matching ressources OK → Voyage PUBLIE. "
                    + "Sinon → VoyageBrouillon (INCOMPLET ou PRET).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Génération effectuée",
                    content = @Content(schema = @Schema(implementation = GenerationResultDTO.class))),
            @ApiResponse(responseCode = "400", description = "Requête invalide"),
            @ApiResponse(responseCode = "404", description = "Ligne ou agence introuvable")
    })
    @PostMapping("/generer-unitaire")
    public Mono<ResponseEntity<GenerationResultDTO>> genererUnitaire(
            @Valid @RequestBody GenerationUnitaireRequestDTO request) {
        return getCurrentUserId()
                .flatMap(userId -> generationUseCase.genererUnitaire(request, userId))
                .map(ResponseEntity::ok);
    }

    @Operation(summary = "Prévisualiser le matching ressources pour une semaine",
            description = "Ne crée rien — retourne juste le statut prévu (PUBLIE | INCOMPLET) pour chaque ligne.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Preview généré",
                    content = @Content(schema = @Schema(implementation = MatchingPreviewResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Agence introuvable")
    })
    @PostMapping("/matching-preview")
    public Mono<ResponseEntity<MatchingPreviewResponseDTO>> matchingPreview(
            @Valid @RequestBody GenerationSemaineRequestDTO request) {
        return generationUseCase.matchingPreview(request)
                .map(ResponseEntity::ok);
    }

    @Operation(summary = "Générer tous les voyages d'une semaine pour une liste de lignes",
            description = "Publie ceux dont le matching est complet, brouillonne les incomplets.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Génération effectuée",
                    content = @Content(schema = @Schema(implementation = GenerationSemaineResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Agence introuvable")
    })
    @PostMapping("/generer-semaine")
    public Mono<ResponseEntity<GenerationSemaineResponseDTO>> genererSemaine(
            @Valid @RequestBody GenerationSemaineRequestDTO request) {
        return getCurrentUserId()
                .flatMap(userId -> generationUseCase.genererSemaine(request, userId))
                .map(ResponseEntity::ok);
    }
}

