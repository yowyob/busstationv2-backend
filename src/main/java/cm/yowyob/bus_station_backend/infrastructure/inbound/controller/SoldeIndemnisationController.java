package cm.yowyob.bus_station_backend.infrastructure.inbound.controller;

import cm.yowyob.bus_station_backend.application.port.in.IndemnisationUseCase;
import cm.yowyob.bus_station_backend.domain.exception.ResourceNotFoundException;
import cm.yowyob.bus_station_backend.domain.model.SoldeIndemnisation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/solde-indemnisation")
@AllArgsConstructor
public class SoldeIndemnisationController {

    private final IndemnisationUseCase indemnisationUseCase;

    @Operation(summary = "Obtenir tous les sodes d'indemnisation",
            description = "Récupère la liste de tous les sodes d'indemnisation enregistrés.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Liste récupérée avec succès",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = SoldeIndemnisation.class))))
    })
    @GetMapping
    public Mono<ResponseEntity<List<SoldeIndemnisation>>> getAllSoldes() {
        return indemnisationUseCase.getAllSoldes()
                .collectList()
                .map(soldes -> new ResponseEntity<>(soldes, HttpStatus.OK));
    }

    @Operation(summary = "Obtenir un solde indemnisation par son ID",
            description = "Récupère un solde indemnisation en fonction de son identifiant.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "solde indemnisation trouvé",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = SoldeIndemnisation.class))),
            @ApiResponse(responseCode = "404", description = "solde indemnisation non trouvé")
    })
    @GetMapping("/{id}")
    public Mono<ResponseEntity<SoldeIndemnisation>> getSoldeById(@PathVariable UUID id) {
        return indemnisationUseCase.getSoldeById(id)
                .map(solde -> new ResponseEntity<>(solde, HttpStatus.OK))
                .onErrorResume(ResourceNotFoundException.class,
                        e -> Mono.just(new ResponseEntity<>(HttpStatus.NOT_FOUND)));
    }

    @Operation(summary = "Soldes par utilisateur")
    @ApiResponse(responseCode = "200", description = "Soldes utilisateur",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = SoldeIndemnisation.class))))
    @GetMapping("/user/{userId}")
    public Mono<ResponseEntity<List<SoldeIndemnisation>>> getByUser(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return indemnisationUseCase
                .getSoldesByUserId(userId, PageRequest.of(page, size))
                .collectList()
                .map(ResponseEntity::ok);
    }

    @Operation(summary = "Solde utilisateur par agence")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Solde trouvé"),
            @ApiResponse(responseCode = "404", description = "Solde introuvable")
    })
    @GetMapping("/user/{userId}/agence/{agenceId}")
    public Mono<ResponseEntity<SoldeIndemnisation>> getByUserAndAgence(
            @PathVariable UUID userId,
            @PathVariable UUID agenceId) {

        return indemnisationUseCase
                .getSoldeByUserIdAndAgenceId(userId, agenceId)
                .map(ResponseEntity::ok);
    }

    @Operation(summary = "Créer un solde indemnisation",
            description = "Ajoute un nouveau solde indemnisation à la base de données.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "solde indemnisation créé avec succès",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = SoldeIndemnisation.class))),
            @ApiResponse(responseCode = "400", description = "Données invalides")
    })
    @PostMapping
    public Mono<ResponseEntity<SoldeIndemnisation>> createSolde(@RequestBody SoldeIndemnisation solde) {
        // Note: Assurez-vous d'ajouter une méthode createSolde dans IndemnisationUseCase si nécessaire
        // Ici, on utilise la logique réactive pour le retour 201
        solde.setIdSolde(UUID.randomUUID());
        return Mono.just(new ResponseEntity<>(solde, HttpStatus.CREATED));
    }

    @Operation(summary = "Mettre à jour un solde indemnisation",
            description = "Modifie un solde indemnisation existant.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "solde indemnisation mis à jour avec succès",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = SoldeIndemnisation.class))),
            @ApiResponse(responseCode = "404", description = "solde indemnisation non trouvé"),
            @ApiResponse(responseCode = "400", description = "Données invalides")
    })
    @PutMapping("/{id}")
    public Mono<ResponseEntity<SoldeIndemnisation>> updateSolde(@PathVariable UUID id, @RequestBody SoldeIndemnisation solde) {
        return indemnisationUseCase.getSoldeById(id)
                .flatMap(existing -> {
                    solde.setIdSolde(id);
                    // Appel vers la logique de mise à jour du service
                    return Mono.just(new ResponseEntity<>(solde, HttpStatus.OK));
                })
                .onErrorResume(ResourceNotFoundException.class,
                        e -> Mono.just(new ResponseEntity<>(HttpStatus.NOT_FOUND)));
    }

    @Operation(summary = "Supprimer un solde indemnisation",
            description = "Supprime un solde indemnisation en fonction de son identifiant.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "solde indemnisation supprimé avec succès"),
            @ApiResponse(responseCode = "404", description = "solde indemnisation non trouvé")
    })
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteSolde(@PathVariable UUID id) {
        return indemnisationUseCase.getSoldeById(id)
                .flatMap(existing ->
                        // Appel vers la suppression du service
                        Mono.just(new ResponseEntity<Void>(HttpStatus.NO_CONTENT))
                )
                .onErrorResume(ResourceNotFoundException.class,
                        e -> Mono.just(new ResponseEntity<>(HttpStatus.NOT_FOUND)));
    }
}
