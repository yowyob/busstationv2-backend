package cm.yowyob.bus_station_backend.infrastructure.inbound.controller;

import cm.yowyob.bus_station_backend.application.dto.agence.AgenceVoyageDTO;
import cm.yowyob.bus_station_backend.application.dto.agence.AgenceVoyagePreviewDTO;
import cm.yowyob.bus_station_backend.application.dto.agence.AgenceVoyageResponseDTO;
import cm.yowyob.bus_station_backend.application.port.in.AgenceUseCase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import static cm.yowyob.bus_station_backend.infrastructure.util.SecurityUtils.getCurrentUserId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import cm.yowyob.bus_station_backend.application.dto.agence.MoyensPaiementDTO;
import cm.yowyob.bus_station_backend.application.dto.agence.RessourcesDefautDTO;
import cm.yowyob.bus_station_backend.application.dto.agence.UpdateStatutAgenceDTO;
import cm.yowyob.bus_station_backend.application.port.in.PlanningUseCase;
import cm.yowyob.bus_station_backend.application.dto.planning.PlanningVoyagePreviewDTO;
import cm.yowyob.bus_station_backend.application.port.in.VoyageBrouillonUseCase;
import cm.yowyob.bus_station_backend.application.dto.voyage.VoyageBrouillonResponseDTO;


import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/agence")
@AllArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class AgenceVoyageController {

        private final AgenceUseCase agenceUseCase;
        private final PlanningUseCase planningUseCase;
        private final VoyageBrouillonUseCase voyageBrouillonUseCase;

        @Operation(summary = "Create a new travel agency")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "201", description = "Travel agency created successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AgenceVoyageDTO.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid input or duplicate agency names"),
                        @ApiResponse(responseCode = "404", description = "User not found")
        })
        @PostMapping
        public Mono<ResponseEntity<AgenceVoyageResponseDTO>> createAgence(@Valid @RequestBody AgenceVoyageDTO agenceDTO) {
                return agenceUseCase.createAgence(agenceDTO)
                                .map(createdAgence -> new ResponseEntity<>(createdAgence, HttpStatus.CREATED));
                // .onErrorResume(ResourceNotFoundException.class, e -> Mono.error(new
                // ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage())))
                // .onErrorResume(Exception.class, e -> Mono.error(new
                // ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage())));
        }

        @Operation(summary = "Update an existing travel agency")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Travel agency updated successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AgenceVoyageDTO.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid input or duplicate agency names"),
                        @ApiResponse(responseCode = "404", description = "Agency not found")
        })
        @PatchMapping("/{id}")
        public Mono<ResponseEntity<AgenceVoyageDTO>> updateAgence(@PathVariable("id") UUID agencyId,
                        @RequestBody AgenceVoyageDTO agenceDTO) {
                return getCurrentUserId()
                                .flatMap(userId -> agenceUseCase.updateAgence(agencyId, agenceDTO, userId))
                                .map(updatedAgence -> new ResponseEntity<>(updatedAgence, HttpStatus.OK));
                // .onErrorResume(ResourceNotFoundException.class, e -> Mono.error(new
                // ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage())))
                // .onErrorResume(UnauthorizeException.class, e -> Mono.error(new
                // ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage())))
                // .onErrorResume(Exception.class, e -> Mono.error(new
                // ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage())));
        }

        @Operation(summary = "Delete a travel agency")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "204", description = "Travel agency deleted successfully"),
                        @ApiResponse(responseCode = "404", description = "Agency not found")
        })
        @DeleteMapping("/{id}")
        public Mono<ResponseEntity<Void>> deleteAgence(@PathVariable("id") UUID agencyId) {
                return getCurrentUserId()
                                .flatMap(userId -> agenceUseCase.deleteAgenceVoyage(agencyId, userId))
                                .then(Mono.just(new ResponseEntity<Void>(HttpStatus.NO_CONTENT)));
                // .onErrorResume(ResourceNotFoundException.class, e -> Mono.error(new
                // ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage())))
                // .onErrorResume(UnauthorizeException.class, e -> Mono.error(new
                // ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage())));
        }

        @Operation(summary = "Get travel agency details by ID")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Travel agency details retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AgenceVoyageResponseDTO.class))),
                        @ApiResponse(responseCode = "404", description = "Agency not found")
        })
        @GetMapping("/{id}")
        public Mono<ResponseEntity<AgenceVoyageResponseDTO>> getAgenceById(@PathVariable UUID id) {
                return agenceUseCase.getAgenceById(id)
                                .map(ResponseEntity::ok);
        }

        @Operation(summary = "Get all travel agencies")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Travel agencies retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AgenceVoyageResponseDTO.class)))
        })
        @GetMapping
        public Mono<ResponseEntity<Page<AgenceVoyageResponseDTO>>> getAllAgences(
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size) {
                return agenceUseCase.getAllAgences(PageRequest.of(page, size))
                                .map(ResponseEntity::ok);
        }

        @Operation(summary = "Retouner une agence d evoyage à partir de l'id du chef d'agence")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Agence trouvée", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AgenceVoyageDTO.class))),
                        @ApiResponse(responseCode = "404", description = "Agence non trouvée")
        })
        @GetMapping("/chef-agence/{id}")
        public Mono<ResponseEntity<AgenceVoyageResponseDTO>> getChefAgenceById(@PathVariable UUID id) {
                return agenceUseCase.getAgenceByChefAgenceId(id)
                                .map(agence -> new ResponseEntity<>(agence, HttpStatus.OK));
                // .onErrorResume(ResourceNotFoundException.class, e -> Mono.just(new
                // ResponseEntity<>(HttpStatus.NOT_FOUND)));
        }

        @Operation(summary = "Récupérer les agences de voyage d'une gare routière")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Agences trouvées", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AgenceVoyagePreviewDTO.class))),
                        @ApiResponse(responseCode = "404", description = "Gare routière non trouvée")
        })
        @GetMapping("/gare-routiere/{gareRoutiereId}")
        public Flux<AgenceVoyagePreviewDTO> getAgencesByGareRoutiereId(@PathVariable UUID gareRoutiereId) {
                return agenceUseCase.getAgencesByGareRoutiereId(gareRoutiereId);
        }

        // ============================================================
        // STATUT (BSM suspend/réactive une agence)
        // ============================================================

        @Operation(summary = "BSM : suspendre ou réactiver une agence")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Statut mis à jour"),
                        @ApiResponse(responseCode = "404", description = "Agence non trouvée")
        })
        @PutMapping("/{id}/statut")
        public Mono<ResponseEntity<AgenceVoyageResponseDTO>> updateStatutAgence(
                        @PathVariable("id") UUID agenceId,
                        @Valid @RequestBody UpdateStatutAgenceDTO body) {
                return getCurrentUserId()
                                .flatMap(userId -> agenceUseCase.updateStatutAgence(
                                                agenceId, body.active(), body.motif(), userId))
                                .map(ResponseEntity::ok);
        }

        // ============================================================
        // VUE PUBLIQUE
        // ============================================================

        @Operation(summary = "Vue publique d'une agence (pour les usagers)")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Agence trouvée"),
                        @ApiResponse(responseCode = "404", description = "Agence non trouvée")
        })
        @GetMapping("/{id}/public")
        public Mono<ResponseEntity<AgenceVoyageResponseDTO>> getAgencePublic(@PathVariable("id") UUID agenceId) {
                return agenceUseCase.getAgenceById(agenceId)
                                .map(ResponseEntity::ok);
        }

        // ============================================================
        // MOYENS DE PAIEMENT
        // ============================================================

        @Operation(summary = "Configurer les moyens de paiement de l'agence")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Moyens de paiement mis à jour"),
                        @ApiResponse(responseCode = "404", description = "Agence non trouvée")
        })
        @PutMapping("/{id}/moyens-paiement")
        public Mono<ResponseEntity<AgenceVoyageResponseDTO>> updateMoyensPaiement(
                        @PathVariable("id") UUID agenceId,
                        @Valid @RequestBody MoyensPaiementDTO body) {
                return getCurrentUserId()
                                .flatMap(userId -> agenceUseCase.updateMoyensPaiement(
                                                agenceId, body.moyensPaiement(), userId))
                                .map(ResponseEntity::ok);
        }

        // ============================================================
        // RESSOURCES PAR DÉFAUT
        // ============================================================

        @Operation(summary = "Configurer le véhicule et le chauffeur par défaut")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Ressources par défaut mises à jour"),
                        @ApiResponse(responseCode = "404", description = "Agence non trouvée")
        })
        @PutMapping("/{id}/ressources-defaut")
        public Mono<ResponseEntity<AgenceVoyageResponseDTO>> updateRessourcesDefaut(
                        @PathVariable("id") UUID agenceId,
                        @RequestBody RessourcesDefautDTO body) {
                return getCurrentUserId()
                                .flatMap(userId -> agenceUseCase.updateRessourcesDefaut(
                                                agenceId, body.vehiculeIdDefaut(), body.chauffeurIdDefaut(), userId))
                                .map(ResponseEntity::ok);
        }

        // ============================================================
        // LIGNES DE SERVICE / BROUILLONS
        // ============================================================

        @Operation(summary = "Lister les lignes de service (planning) d'une agence")
        @GetMapping("/{id}/lignes-service")
        public Flux<PlanningVoyagePreviewDTO> getLignesServiceByAgence(@PathVariable("id") UUID agenceId) {
                return planningUseCase.getPlanningsByAgence(agenceId);
        }

        @Operation(summary = "Lister les voyages brouillons d'une agence (filtrable par statut)")
        @GetMapping("/{id}/brouillons")
        public Flux<VoyageBrouillonResponseDTO> getBrouillonsByAgence(
                        @PathVariable("id") UUID agenceId,
                        @RequestParam(required = false) String statut) {
                return voyageBrouillonUseCase.listByAgence(agenceId, statut);
        }
}

