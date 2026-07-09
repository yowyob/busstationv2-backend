package cm.yowyob.bus_station_backend.infrastructure.inbound.controller;

import cm.yowyob.bus_station_backend.application.dto.vehicule.VehiculeDTO;
import cm.yowyob.bus_station_backend.application.port.in.AgenceUseCase;
import cm.yowyob.bus_station_backend.domain.exception.ResourceNotFoundException;
import cm.yowyob.bus_station_backend.domain.exception.UnauthorizeException;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import cm.yowyob.bus_station_backend.application.dto.disponibilite.DisponibiliteResponseDTO;
import cm.yowyob.bus_station_backend.application.port.in.DisponibiliteUseCase;

import java.util.List;
import java.util.UUID;

import static cm.yowyob.bus_station_backend.infrastructure.util.SecurityUtils.getCurrentUserId;

@RestController
@RequestMapping("/vehicule")
@AllArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class VehiculeController {

        private final AgenceUseCase agenceUseCase;
        private final DisponibiliteUseCase disponibiliteUseCase;

        @Operation(summary = "Obtenir tous les vehicules", description = "Récupère la liste de tous les vehicules enregistrés.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Liste récupérée avec succès", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = VehiculeDTO.class))))
        })
        @GetMapping
        public Mono<ResponseEntity<Page<VehiculeDTO>>> getAllVehicules(
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size) {

                // Note: L'ancien backend permettait de voir TOUS les véhicules.
                // Dans l'architecture réactive, on peut utiliser le port existant sans filtrage
                // d'agence
                return agenceUseCase.getVehiculesByAgence(null)
                                .collectList()
                                .map(list -> {
                                        PageRequest pageable = PageRequest.of(page, size);
                                        int start = (int) pageable.getOffset();
                                        int end = Math.min((start + pageable.getPageSize()), list.size());
                                        return new ResponseEntity<>(
                                                        new PageImpl<>(list.subList(start, end), pageable, list.size()),
                                                        HttpStatus.OK);
                                });
        }

        @Operation(summary = "Obtenir tous les vehicules d'une agence", description = "Récupère la liste de tous les vehicules de l'agence dont l'id est passé dans la route.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Liste récupérée avec succès", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = VehiculeDTO.class))))
        })
        @GetMapping("/agence/{idAgence}")
        public Mono<ResponseEntity<List<VehiculeDTO>>> getAllVehiculesForAgence(@PathVariable UUID idAgence) {
                return agenceUseCase.getVehiculesByAgence(idAgence)
                                .collectList()
                                .map(ResponseEntity::ok);
        }

        @Operation(summary = "Obtenir un vehicule par ID", description = "Récupère un vehicule en fonction de son identifiant.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Vehicule trouvé", content = @Content(mediaType = "application/json", schema = @Schema(implementation = VehiculeDTO.class))),
                        @ApiResponse(responseCode = "404", description = "Vehicule non trouvé")
        })
        @GetMapping("/{id}")
        public Mono<ResponseEntity<VehiculeDTO>> getVehiculeById(@PathVariable UUID id) {
                return agenceUseCase.getVehiculeById(id)
                                .map(ResponseEntity::ok)
                                .onErrorResume(ResourceNotFoundException.class,
                                                e -> Mono.just(ResponseEntity.notFound().build()));
        }

        @Operation(summary = "Créer un vehicule", description = "Ajoute un nouveau vehicule à la base de données.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "201", description = "Vehicule créé avec succès", content = @Content(mediaType = "application/json", schema = @Schema(implementation = VehiculeDTO.class))),
                        @ApiResponse(responseCode = "400", description = "Données invalides")
        })
        @PostMapping
        public Mono<ResponseEntity<VehiculeDTO>> createVehicule(@Valid @RequestBody VehiculeDTO vehiculeDTO) {
                return getCurrentUserId()
                                .flatMap(userId -> agenceUseCase.addVehicule(vehiculeDTO.getIdAgenceVoyage(),
                                                vehiculeDTO, userId))
                                .map(created -> new ResponseEntity<>(created, HttpStatus.CREATED))
                                .onErrorResume(UnauthorizeException.class,
                                                e -> Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN,
                                                                e.getMessage())))
                                .onErrorResume(Exception.class, e -> Mono.error(
                                                new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage())));
        }

        @Operation(summary = "Mettre à jour un vehicule", description = "Modifie un vehicule existant.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Vehicule mis à jour avec succès", content = @Content(mediaType = "application/json", schema = @Schema(implementation = VehiculeDTO.class))),
                        @ApiResponse(responseCode = "404", description = "vehicule non trouvé"),
                        @ApiResponse(responseCode = "400", description = "Données invalides")
        })
        @PutMapping("/{id}")
        public Mono<ResponseEntity<VehiculeDTO>> updateVehicule(@PathVariable UUID id,
                        @RequestBody VehiculeDTO vehiculeDTO) {
                return getCurrentUserId()
                                .flatMap(userId -> agenceUseCase.updateVehicule(id, vehiculeDTO, userId))
                                .map(ResponseEntity::ok)
                                .onErrorResume(ResourceNotFoundException.class,
                                                e -> Mono.just(ResponseEntity.notFound().build()))
                                .onErrorResume(UnauthorizeException.class,
                                                e -> Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN,
                                                                e.getMessage())))
                                .onErrorResume(Exception.class, e -> Mono.error(
                                                new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage())));
        }

        @Operation(summary = "Supprimer un vehicule", description = "Supprime un vehicule en fonction de son identifiant.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "204", description = "Vehicule supprimé avec succès"),
                        @ApiResponse(responseCode = "404", description = "Vehicule non trouvé")
        })
        @DeleteMapping("/{id}")
        public Mono<ResponseEntity<Void>> deleteVehicule(@PathVariable UUID id) {
                return getCurrentUserId()
                                .flatMap(userId -> agenceUseCase.deleteVehicule(id, userId))
                                .then(Mono.just(new ResponseEntity<Void>(HttpStatus.NO_CONTENT)))
                                .onErrorResume(ResourceNotFoundException.class,
                                                e -> Mono.just(ResponseEntity.notFound().build()))
                                .onErrorResume(UnauthorizeException.class, e -> Mono.error(
                                                new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage())));
        }

        @Operation(summary = "Vérifier la disponibilité d'un véhicule à une date/heure")
        @GetMapping("/{id}/disponibilite")
        public Mono<ResponseEntity<DisponibiliteResponseDTO>> getVehiculeDisponibilite(
                        @PathVariable UUID id,
                        @RequestParam String date,
                        @RequestParam(required = false) String heure) {
                return disponibiliteUseCase.checkVehiculeDisponibilite(id, date, heure)
                                .map(ResponseEntity::ok);
        }
}
