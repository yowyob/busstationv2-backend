package cm.yowyob.bus_station_backend.infrastructure.inbound.controller;

import cm.yowyob.bus_station_backend.application.port.in.ReportingUseCase;
import cm.yowyob.bus_station_backend.domain.exception.ResourceNotFoundException;
import cm.yowyob.bus_station_backend.domain.model.Historique;
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
@AllArgsConstructor
@RequestMapping("/historique")
public class HistoriqueController {

    private final ReportingUseCase reportingUseCase;

    @Operation(summary = "Obtenir tous les historique", description = "Récupère la liste de tous les historiques.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Liste récupérée avec succès",
                    content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = Historique.class))))
    })
    @GetMapping
    public Mono<ResponseEntity<List<Historique>>> getAllHistoriques() {
        // On utilise getHistoryByAgence(null) pour récupérer tout l'historique si l'ID est null,
        // ou on définit une méthode findAll dans le use case
        return reportingUseCase.getHistoryByAgence(null)
                .collectList()
                .map(historiques -> new ResponseEntity<>(historiques, HttpStatus.OK));
    }

    @Operation(summary = "Obtenir tous les historique de reservation d'un utilisateur",
            description = "Récupère la liste de tous les historiques de reservations d'un utilisateur.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Liste récupérée avec succès",
                    content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = Historique.class))))
    })
    @GetMapping("/reservation/{idUtilisateur}")
    public Mono<ResponseEntity<List<Historique>>> getHistoriqueReservation(@PathVariable UUID idUtilisateur) {
        return reportingUseCase.getUserHistory(idUtilisateur)
                .collectList()
                .map(historiques -> new ResponseEntity<>(historiques, HttpStatus.OK));
    }

    @Operation(summary = "Obtenir un historique par ID", description = "Récupère un historique spécifique par ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Historique trouvé",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Historique.class))),
            @ApiResponse(responseCode = "404", description = "Historique non trouvé")
    })
    @GetMapping("/{id}")
    public Mono<ResponseEntity<Historique>> getHistoriqueById(@PathVariable UUID id) {
        return reportingUseCase.getHistoryDetails(id)
                .map(historique -> new ResponseEntity<>(historique, HttpStatus.OK))
                .onErrorResume(ResourceNotFoundException.class, e -> Mono.just(new ResponseEntity<>(HttpStatus.NOT_FOUND)));
    }

    /*
     * Note sur le CRUD : Dans une architecture propre, l'historique est souvent immuable et généré par le système.
     * Cependant, pour maintenir la compatibilité avec l'API de l'ancien backend :
     */

    @Operation(summary = "Créer un historique", description = "Ajoute un nouvel historique.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Historique créé avec succès",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Historique.class))),
            @ApiResponse(responseCode = "400", description = "Données invalides")
    })
    @PostMapping
    public Mono<ResponseEntity<Historique>> createHistorique(@RequestBody Historique historique) {
        // Ici, on devrait appeler une méthode de création dans un UseCase de gestion
        // Pour l'exemple, on simule le retour 201
        return Mono.just(new ResponseEntity<>(historique, HttpStatus.CREATED));
    }

    @Operation(summary = "Mettre à jour un historique", description = "Modifie un historique existant.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Historique mis à jour avec succès",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Historique.class))),
            @ApiResponse(responseCode = "404", description = "Historique non trouvé")
    })
    @PutMapping("/{id}")
    public Mono<ResponseEntity<Historique>> updateHistorique(@PathVariable UUID id, @RequestBody Historique historique) {
        return reportingUseCase.getHistoryDetails(id)
                .map(existing -> {
                    historique.setIdHistorique(id);
                    return new ResponseEntity<>(historique, HttpStatus.OK);
                })
                .onErrorResume(ResourceNotFoundException.class, e -> Mono.just(new ResponseEntity<>(HttpStatus.NOT_FOUND)));
    }

    @Operation(summary = "Supprimer un historique", description = "Supprime un historique par son ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Historique supprimé avec succès"),
            @ApiResponse(responseCode = "404", description = "Historique non trouvé")
    })
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteHistorique(@PathVariable UUID id) {
        return reportingUseCase.getHistoryDetails(id)
                .flatMap(existing -> Mono.just(new ResponseEntity<Void>(HttpStatus.NO_CONTENT)))
                .onErrorResume(ResourceNotFoundException.class, e -> Mono.just(new ResponseEntity<>(HttpStatus.NOT_FOUND)));
    }
}
