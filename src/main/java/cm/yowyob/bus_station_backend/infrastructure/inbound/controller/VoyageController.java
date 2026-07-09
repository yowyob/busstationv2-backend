package cm.yowyob.bus_station_backend.infrastructure.inbound.controller;

import cm.yowyob.bus_station_backend.application.dto.voyage.VoyageCreateRequestDTO;
import cm.yowyob.bus_station_backend.application.dto.voyage.VoyageDTO;
import cm.yowyob.bus_station_backend.application.dto.voyage.VoyageDetailsDTO;
import cm.yowyob.bus_station_backend.application.dto.voyage.VoyagePreviewDTO;
import cm.yowyob.bus_station_backend.application.port.in.VoyageUseCase;
import cm.yowyob.bus_station_backend.domain.exception.ResourceNotFoundException;
import static cm.yowyob.bus_station_backend.infrastructure.util.SecurityUtils.getCurrentUserId;
import cm.yowyob.bus_station_backend.domain.model.Voyage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/voyage")
@AllArgsConstructor
public class VoyageController {

        private final VoyageUseCase voyageUseCase;

        @Operation(summary = "Obtenir tous les voyages", description = "Récupère la liste de tous les voyages (champs stricts pour le preview) enregistrés.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Liste récupérée avec succès", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = VoyagePreviewDTO.class))))
        })
        @GetMapping
       public Mono<Page<VoyagePreviewDTO>> getAllVoyages(
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "12") int size) {
                return voyageUseCase.getAllVoyagesPreview(PageRequest.of(page, size));
        }

        @Operation(summary = "Obtenir les détails d'un voyage par ID", description = "Récupère un voyage en fonction de son identifiant.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Voyage trouvé", content = @Content(mediaType = "application/json", schema = @Schema(implementation = VoyageDetailsDTO.class))),
                        @ApiResponse(responseCode = "404", description = "Voyage non trouvé")
        })
        @GetMapping("/{id}")
        public Mono<ResponseEntity<VoyageDetailsDTO>> getVoyageById(@PathVariable UUID id) {
                return voyageUseCase.getVoyageById(id)
                                .map(ResponseEntity::ok)
                                .onErrorResume(ResourceNotFoundException.class,
                                                e -> Mono.just(ResponseEntity.notFound().build()));
        }

        @Operation(summary = "Créer un voyage", description = "Ajoute un nouveau voyage à la base de données.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "201", description = "Voyage créé avec succès", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Voyage.class))),
                        @ApiResponse(responseCode = "400", description = "Données invalides")
        })
        @PostMapping
        @SecurityRequirement(name = "bearerAuth")
        public Mono<ResponseEntity<VoyageDetailsDTO>> createVoyage(@RequestBody @Valid VoyageCreateRequestDTO voyage) {
                return getCurrentUserId()
                                .flatMap(userId -> voyageUseCase.createVoyage(voyage, userId))
                                .map(createdVoyage -> new ResponseEntity<>(createdVoyage, HttpStatus.CREATED))
                                .onErrorResume(Exception.class, e -> Mono.error(
                                                new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage())));
        }

        @Operation(summary = "Mettre à jour un voyage", description = "Modifie un voyage existant.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Voyage mis à jour avec succès", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Voyage.class))),
                        @ApiResponse(responseCode = "404", description = "Voyage non trouvé"),
                        @ApiResponse(responseCode = "400", description = "Données invalides")
        })
        @PutMapping("/{id}")
        @SecurityRequirement(name = "bearerAuth")
        public Mono<ResponseEntity<VoyageDetailsDTO>> updateVoyage(@PathVariable UUID id,
                        @RequestBody VoyageDTO voyageDTO) {
                return getCurrentUserId()
                                .flatMap(userId -> voyageUseCase.updateVoyage(id, voyageDTO, userId))
                                .map(ResponseEntity::ok)
                                .onErrorResume(ResourceNotFoundException.class,
                                                e -> Mono.just(ResponseEntity.notFound().build()))
                                .onErrorResume(Exception.class, e -> Mono.error(
                                                new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage())));
        }

        @Operation(summary = "Supprimer un voyage", description = "Supprime un voyage en fonction de son identifiant.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "204", description = "Voyage supprimé avec succès"),
                        @ApiResponse(responseCode = "404", description = "Voyage non trouvé")
        })
        @DeleteMapping("/{id}")
        @SecurityRequirement(name = "bearerAuth")
        public Mono<ResponseEntity<Void>> deleteVoyage(@PathVariable UUID id) {
                return getCurrentUserId()
                                .flatMap(userId -> voyageUseCase.deleteVoyage(id, userId))
                                .then(Mono.just(new ResponseEntity<Void>(HttpStatus.NO_CONTENT)))
                                .onErrorResume(ResourceNotFoundException.class,
                                                e -> Mono.just(ResponseEntity.notFound().build()));
        }

        @Operation(summary = "Obtenir tous les voyages d'une agence", description = "Récupère la liste de tous les voyages d'une agence spécifique.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Liste récupérée avec succès", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = VoyagePreviewDTO.class)))),
                        @ApiResponse(responseCode = "404", description = "Agence non trouvée"),
                        @ApiResponse(responseCode = "500", description = "Erreur interne du serveur")
        })
        @GetMapping("/agence/{agenceId}")
        public Mono<ResponseEntity<Page<VoyagePreviewDTO>>> getAllVoyagesByAgence(
                        @PathVariable UUID agenceId,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "12") int size) {

                return voyageUseCase.getVoyagesByAgence(agenceId, PageRequest.of(page, size))
                                .map(ResponseEntity::ok)
                                .onErrorResume(ResourceNotFoundException.class,
                                                e -> Mono.just(ResponseEntity.notFound().build()))
                                .onErrorResume(Exception.class, e -> Mono
                                                .just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()));
        }

        @Operation(summary = "Obtenir tous les voyages d'une gare routière", description = "Récupère la liste de tous les voyages (départs et arrivées) d'une gare routière spécifique.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Liste récupérée avec succès", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = VoyagePreviewDTO.class)))),
                        @ApiResponse(responseCode = "404", description = "Gare routière non trouvée"),
                        @ApiResponse(responseCode = "500", description = "Erreur interne du serveur")
        })
        @GetMapping("/gare/{gareId}")
        public Mono<ResponseEntity<Page<VoyagePreviewDTO>>> getAllVoyagesByGareRoutiere(
                        @PathVariable UUID gareId,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "12") int size) {

                return voyageUseCase.getVoyagesByGareRoutiere(gareId, PageRequest.of(page, size))
                                .map(ResponseEntity::ok)
                                .onErrorResume(ResourceNotFoundException.class,
                                                e -> Mono.just(ResponseEntity.notFound().build()))
                                .onErrorResume(Exception.class, e -> Mono
                                                .just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()));
        }

        @Operation(summary = "Publier un voyage")
        @PutMapping("/{id}/publier")
        @SecurityRequirement(name = "bearerAuth")
        public Mono<ResponseEntity<VoyageDetailsDTO>> publierVoyage(@PathVariable UUID id) {
                return getCurrentUserId()
                                .flatMap(userId -> voyageUseCase.updateVoyageStatus(id, "PUBLIE", userId))
                                .map(ResponseEntity::ok)
                                .onErrorResume(ResourceNotFoundException.class,
                                                e -> Mono.just(ResponseEntity.notFound().build()));
        }

        @Operation(summary = "Dépublier un voyage")
        @PutMapping("/{id}/depublier")
        @SecurityRequirement(name = "bearerAuth")
        public Mono<ResponseEntity<VoyageDetailsDTO>> depublierVoyage(@PathVariable UUID id) {
                return getCurrentUserId()
                                .flatMap(userId -> voyageUseCase.updateVoyageStatus(id, "EN_ATTENTE", userId))
                                .map(ResponseEntity::ok)
                                .onErrorResume(ResourceNotFoundException.class,
                                                e -> Mono.just(ResponseEntity.notFound().build()));
        }

        @Operation(summary = "Annuler un voyage")
        @PutMapping("/{id}/annuler")
        @SecurityRequirement(name = "bearerAuth")
        public Mono<ResponseEntity<VoyageDetailsDTO>> annulerVoyage(@PathVariable UUID id) {
                return getCurrentUserId()
                                .flatMap(userId -> voyageUseCase.updateVoyageStatus(id, "ANNULE", userId))
                                .map(ResponseEntity::ok)
                                .onErrorResume(ResourceNotFoundException.class,
                                                e -> Mono.just(ResponseEntity.notFound().build()));
        }

        @Operation(summary = "Voyages similaires")
        @GetMapping("/{id}/similaires")
        public Mono<ResponseEntity<java.util.List<VoyagePreviewDTO>>> getSimilaires(
                        @PathVariable UUID id,
                        @RequestParam(defaultValue = "6") int limit) {
                return voyageUseCase.getVoyagesSimilaires(id, limit)
                                .collectList()
                                .map(ResponseEntity::ok);
        }

        @Operation(summary = "Voyages publiés d'une agence (public)")
        @GetMapping("/agence/{agenceId}/public")
        public Mono<ResponseEntity<Page<VoyagePreviewDTO>>> getVoyagesPublicsByAgence(
                        @PathVariable UUID agenceId,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "12") int size) {
                return voyageUseCase.getVoyagesPublicsByAgence(agenceId, PageRequest.of(page, size))
                                .map(ResponseEntity::ok);
        }

        @Operation(summary = "Recherche de voyages avec filtres")
        @GetMapping("/search")
        public Mono<ResponseEntity<Page<VoyagePreviewDTO>>> searchVoyages(
                        @RequestParam(required = false) String lieuDepart,
                        @RequestParam(required = false) String lieuArrive,
                        @RequestParam(required = false) String date,
                        @RequestParam(required = false) UUID classId,
                        @RequestParam(required = false) UUID agenceId,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "12") int size) {
                return voyageUseCase.searchVoyages(lieuDepart, lieuArrive, date, classId, agenceId, PageRequest.of(page, size))
                                .map(ResponseEntity::ok);
        }
}
