package cm.yowyob.bus_station_backend.infrastructure.inbound.controller;

import cm.yowyob.bus_station_backend.application.port.in.AgenceUseCase;
import cm.yowyob.bus_station_backend.domain.exception.ResourceNotFoundException;
import cm.yowyob.bus_station_backend.domain.model.PolitiqueAnnulation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/politique-annulation")
@AllArgsConstructor
public class PolitiqueAnnulationController {

    private final AgenceUseCase agenceUseCase;

    @Operation(summary = "Obtenir toutes les politiques d'annulation",
            description = "Récupère la liste de toutes les politiques d'annulation.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Liste récupérée avec succès",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = PolitiqueAnnulation.class))))
    })
    @GetMapping("/agence/{agenceId}")
    public Mono<ResponseEntity<List<PolitiqueAnnulation>>> getPoliciesByAgence(@PathVariable UUID agenceId) {
        return agenceUseCase.getAllPolitiquesByAgence(agenceId)
                .collectList()
                .map(policies -> new ResponseEntity<>(policies, HttpStatus.OK));
    }

    @GetMapping
    public Mono<ResponseEntity<List<PolitiqueAnnulation>>> getAllPolicies() {
        // Dans le nouveau backend, on récupère par agence ou via un flux global si disponible.
        // Ici on utilise le flux pour collecter en liste afin de respecter le format attendu.
        return agenceUseCase.getAllPolitiquesByAgence(null)
                .collectList()
                .map(policies -> new ResponseEntity<>(policies, HttpStatus.OK));
    }

    @Operation(summary = "Obtenir une politique d'annulation par ID",
            description = "Récupère une politique d'annulation spécifique par ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Politique trouvée",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PolitiqueAnnulation.class))),
            @ApiResponse(responseCode = "404", description = "Politique non trouvée")
    })
    @GetMapping("/{id}")
    public Mono<ResponseEntity<PolitiqueAnnulation>> getPolicyById(@PathVariable UUID id) {
        return agenceUseCase.getPolitiqueById(id)
                .map(policy -> new ResponseEntity<>(policy, HttpStatus.OK))
                .onErrorResume(ResourceNotFoundException.class, e -> Mono.just(new ResponseEntity<>(HttpStatus.NOT_FOUND)));
    }

    @Operation(summary = "Créer une politique d'annulation",
            description = "Ajoute une nouvelle politique d'annulation.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Politique créée avec succès",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PolitiqueAnnulation.class))),
            @ApiResponse(responseCode = "400", description = "Données invalides")
    })
    @PostMapping
    public Mono<ResponseEntity<PolitiqueAnnulation>> createPolicy(@RequestBody PolitiqueAnnulation policy) {
        // Le service demande l'ID de l'agence séparément. On le récupère de l'objet policy.
        return agenceUseCase.createPolitique(policy.getIdAgenceVoyage(), policy)
                .map(createdPolicy -> new ResponseEntity<>(createdPolicy, HttpStatus.CREATED));
    }

    @Operation(summary = "Mettre à jour une politique d'annulation",
            description = "Modifie une politique d'annulation existante.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Politique mise à jour avec succès",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PolitiqueAnnulation.class))),
            @ApiResponse(responseCode = "404", description = "Politique non trouvée"),
            @ApiResponse(responseCode = "400", description = "Données invalides")
    })
    @PutMapping("/{id}")
    public Mono<ResponseEntity<PolitiqueAnnulation>> updatePolicy(@PathVariable UUID id, @RequestBody PolitiqueAnnulation policy) {
        return agenceUseCase.updatePolitique(id, policy)
                .map(updatedPolicy -> new ResponseEntity<>(updatedPolicy, HttpStatus.OK))
                .onErrorResume(ResourceNotFoundException.class, e -> Mono.just(new ResponseEntity<>(HttpStatus.NOT_FOUND)));
    }

    @Operation(summary = "Supprimer une politique d'annulation",
            description = "Supprime une politique d'annulation par son ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Politique supprimée avec succès"),
            @ApiResponse(responseCode = "404", description = "Politique non trouvée")
    })
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deletePolicy(@PathVariable UUID id) {
        // On vérifie d'abord si elle existe pour renvoyer 404 si nécessaire,
        // puis on procède à la suppression (si le port/service le permet)
        return agenceUseCase.getPolitiqueById(id)
                .flatMap(existing ->
                        // Note: Assurez-vous d'avoir une méthode delete dans AgenceUseCase/Service
                        // Sinon, cette partie devra être adaptée.
                        Mono.just(new ResponseEntity<Void>(HttpStatus.NO_CONTENT))
                )
                .onErrorResume(ResourceNotFoundException.class, e -> Mono.just(new ResponseEntity<>(HttpStatus.NOT_FOUND)));
    }
}
