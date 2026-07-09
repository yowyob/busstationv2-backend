package cm.yowyob.bus_station_backend.infrastructure.inbound.controller;

import cm.yowyob.bus_station_backend.application.dto.planning.*;
import cm.yowyob.bus_station_backend.application.dto.voyage.VoyageDetailsDTO;
import cm.yowyob.bus_station_backend.application.port.in.PlanningUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static cm.yowyob.bus_station_backend.infrastructure.util.SecurityUtils.getCurrentUserId;

@RestController
@RequestMapping("/ligne-service")
@RequiredArgsConstructor
@Tag(name = "Ligne de Service", description = "Gestion des lignes de service (planning récurrent) des agences")
public class PlanningController {

    private final PlanningUseCase planningUseCase;

    // ==================== CRUD Planning ====================

    @Operation(summary = "Créer un nouveau planning de voyage")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Planning créé avec succès",
                    content = @Content(schema = @Schema(implementation = PlanningVoyageDTO.class))),
            @ApiResponse(responseCode = "400", description = "Données invalides"),
            @ApiResponse(responseCode = "403", description = "Non autorisé"),
            @ApiResponse(responseCode = "404", description = "Agence introuvable")
    })
    @PostMapping
    @SecurityRequirement(name = "bearerAuth")
    public Mono<ResponseEntity<PlanningVoyageDTO>> createPlanning(
            @Valid @RequestBody PlanningVoyageDTO dto) {
        return getCurrentUserId()
                .flatMap(userId -> planningUseCase.createPlanning(dto, userId))
                .map(planning -> new ResponseEntity<>(planning, HttpStatus.CREATED));
    }

    @Operation(summary = "Mettre à jour un planning existant")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Planning mis à jour avec succès"),
            @ApiResponse(responseCode = "403", description = "Non autorisé"),
            @ApiResponse(responseCode = "404", description = "Planning introuvable")
    })
    @PatchMapping("/{planningId}")
    @SecurityRequirement(name = "bearerAuth")
    public Mono<ResponseEntity<PlanningVoyageDTO>> updatePlanning(
            @PathVariable UUID planningId,
            @RequestBody PlanningVoyageDTO dto) {
        return getCurrentUserId()
                .flatMap(userId -> planningUseCase.updatePlanning(planningId, dto, userId))
                .map(planning -> new ResponseEntity<>(planning, HttpStatus.OK));
    }

    @Operation(summary = "Supprimer un planning et tous ses créneaux")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Planning supprimé"),
            @ApiResponse(responseCode = "403", description = "Non autorisé"),
            @ApiResponse(responseCode = "404", description = "Planning introuvable")
    })
    @DeleteMapping("/{planningId}")
    @SecurityRequirement(name = "bearerAuth")
    public Mono<ResponseEntity<Void>> deletePlanning(@PathVariable UUID planningId) {
        return getCurrentUserId()
                .flatMap(userId -> planningUseCase.deletePlanning(planningId, userId))
                .then(Mono.just(new ResponseEntity<Void>(HttpStatus.NO_CONTENT)));
    }

    @Operation(summary = "Récupérer un planning par ID (avec créneaux)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Planning trouvé"),
            @ApiResponse(responseCode = "404", description = "Planning introuvable")
    })
    @GetMapping("/{planningId}")
    @SecurityRequirement(name = "bearerAuth")
    public Mono<ResponseEntity<PlanningVoyageDTO>> getPlanningById(@PathVariable UUID planningId) {
        return planningUseCase.getPlanningById(planningId)
                .map(planning -> new ResponseEntity<>(planning, HttpStatus.OK));
    }

    @Operation(summary = "Récupérer tous les plannings d'une agence")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Liste des plannings")
    })
    @GetMapping("/agence/{agenceId}")
    @SecurityRequirement(name = "bearerAuth")
    public Flux<PlanningVoyagePreviewDTO> getPlanningsByAgence(@PathVariable UUID agenceId) {
        return planningUseCase.getPlanningsByAgence(agenceId);
    }

    // ==================== CRUD Creneaux ====================

    @Operation(summary = "Ajouter un créneau à un planning")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Créneau ajouté"),
            @ApiResponse(responseCode = "400", description = "Données invalides ou incohérentes avec le type de récurrence"),
            @ApiResponse(responseCode = "403", description = "Non autorisé"),
            @ApiResponse(responseCode = "404", description = "Planning introuvable")
    })
    @PostMapping("/{planningId}/creneaux")
    @SecurityRequirement(name = "bearerAuth")
    public Mono<ResponseEntity<CreneauPlanningDTO>> addCreneau(
            @PathVariable UUID planningId,
            @Valid @RequestBody CreneauPlanningDTO dto) {
        return getCurrentUserId()
                .flatMap(userId -> planningUseCase.addCreneau(planningId, dto, userId))
                .map(creneau -> new ResponseEntity<>(creneau, HttpStatus.CREATED));
    }

    @Operation(summary = "Mettre à jour un créneau")
    @PatchMapping("/creneaux/{creneauId}")
    @SecurityRequirement(name = "bearerAuth")
    public Mono<ResponseEntity<CreneauPlanningDTO>> updateCreneau(
            @PathVariable UUID creneauId,
            @RequestBody CreneauPlanningDTO dto) {
        return getCurrentUserId()
                .flatMap(userId -> planningUseCase.updateCreneau(creneauId, dto, userId))
                .map(creneau -> new ResponseEntity<>(creneau, HttpStatus.OK));
    }

    @Operation(summary = "Supprimer un créneau")
    @DeleteMapping("/creneaux/{creneauId}")
    @SecurityRequirement(name = "bearerAuth")
    public Mono<ResponseEntity<Void>> deleteCreneau(@PathVariable UUID creneauId) {
        return getCurrentUserId()
                .flatMap(userId -> planningUseCase.deleteCreneau(creneauId, userId))
                .then(Mono.just(new ResponseEntity<Void>(HttpStatus.NO_CONTENT)));
    }

    @Operation(summary = "Récupérer les créneaux d'un planning")
    @GetMapping("/{planningId}/creneaux")
    @SecurityRequirement(name = "bearerAuth")
    public Flux<CreneauPlanningDTO> getCreneauxByPlanning(@PathVariable UUID planningId) {
        return planningUseCase.getCreneauxByPlanning(planningId);
    }

    // ==================== Status Management ====================

    @Operation(summary = "Activer un planning (le rend visible et utilisable)")
    @PatchMapping("/{planningId}/activer")
    @SecurityRequirement(name = "bearerAuth")
    public Mono<ResponseEntity<PlanningVoyageDTO>> activerPlanning(@PathVariable UUID planningId) {
        return getCurrentUserId()
                .flatMap(userId -> planningUseCase.activerPlanning(planningId, userId))
                .map(planning -> new ResponseEntity<>(planning, HttpStatus.OK));
    }

    @Operation(summary = "Désactiver un planning")
    @PatchMapping("/{planningId}/desactiver")
    @SecurityRequirement(name = "bearerAuth")
    public Mono<ResponseEntity<PlanningVoyageDTO>> desactiverPlanning(@PathVariable UUID planningId) {
        return getCurrentUserId()
                .flatMap(userId -> planningUseCase.desactiverPlanning(planningId, userId))
                .map(planning -> new ResponseEntity<>(planning, HttpStatus.OK));
    }

    // ==================== Voyage Generation ====================

    @Operation(summary = "Générer des voyages à partir d'un planning",
            description = "Crée automatiquement des voyages pour chaque créneau actif du planning "
                    + "sur la période spécifiée. Par exemple, pour un planning hebdomadaire avec "
                    + "un créneau le lundi et le mercredi, tous les lundis et mercredis de la période "
                    + "auront un voyage créé.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Voyages générés avec succès"),
            @ApiResponse(responseCode = "400", description = "Planning inactif ou sans créneaux"),
            @ApiResponse(responseCode = "403", description = "Non autorisé"),
            @ApiResponse(responseCode = "404", description = "Planning introuvable")
    })
    @PostMapping("/generate-voyages")
    @SecurityRequirement(name = "bearerAuth")
    public Flux<VoyageDetailsDTO> generateVoyages(
            @Valid @RequestBody GenerateVoyagesFromPlanningDTO dto) {
        return getCurrentUserId()
                .flatMapMany(userId -> planningUseCase.generateVoyagesFromPlanning(dto, userId));
    }

    // ==================== Public Endpoints ====================

    @Operation(summary = "Consulter les plannings actifs d'une agence (public)")
    @GetMapping("/public/agence/{agenceId}")
    public Flux<PlanningVoyagePreviewDTO> getPlanningsActifsByAgence(@PathVariable UUID agenceId) {
        return planningUseCase.getPlanningsActifsByAgence(agenceId);
    }

    @Operation(summary = "Consulter un planning actif par ID (public)")
    @GetMapping("/public/{planningId}")
    public Mono<ResponseEntity<PlanningVoyageDTO>> getPlanningPublic(@PathVariable UUID planningId) {
        return planningUseCase.getPlanningPublicById(planningId)
                .map(planning -> new ResponseEntity<>(planning, HttpStatus.OK));
    }
}
