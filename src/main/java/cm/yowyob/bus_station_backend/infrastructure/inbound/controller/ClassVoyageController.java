package cm.yowyob.bus_station_backend.infrastructure.inbound.controller;

import cm.yowyob.bus_station_backend.application.dto.classVoyage.ClassVoyageDTO;
import cm.yowyob.bus_station_backend.application.dto.classVoyage.ClassVoyageResponseDTO;
import cm.yowyob.bus_station_backend.application.port.in.VoyageUseCase;
import cm.yowyob.bus_station_backend.domain.exception.ResourceNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/class-voyage")
@AllArgsConstructor
public class ClassVoyageController {

    private final VoyageUseCase voyageUseCase;

    @Operation(summary = "Obtenir toutes les classes de voyage",
            description = "Récupère la liste de toutes les classes de voyage enregistrées.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Liste récupérée avec succès",
                    content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ClassVoyageDTO.class))))
    })
    @GetMapping
    public Mono<ResponseEntity<Page<ClassVoyageDTO>>> getAllClassVoyages(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return voyageUseCase.getAllClassVoyages(PageRequest.of(page, size))
                .map(ResponseEntity::ok);
    }

    @Operation(summary = "Obtenir toutes les classes de voyage d'une agence de voyage",
            description = "Récupère la liste de toutes les classes de voyage enregistrées pour une agence de voyage spécifique.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Liste récupérée avec succès",
                    content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ClassVoyageDTO.class))))
    })
    @GetMapping("/agence/{idAgence}")
    @SecurityRequirement(name = "bearerAuth")
    public Mono<ResponseEntity<List<ClassVoyageResponseDTO>>> getAllClassVoyagesByAgence(@PathVariable UUID idAgence) {
        return voyageUseCase.getClassVoyagesByAgence(idAgence)
                .collectList()
                .map(ResponseEntity::ok);
    }

    @Operation(summary = "Obtenir une classe de voyage par ID",
            description = "Récupère une classe de voyage en fonction de son identifiant.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Classe de voyage trouvée",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ClassVoyageDTO.class))),
            @ApiResponse(responseCode = "404", description = "Classe de voyage non trouvée")
    })
    @GetMapping("/{id}")
    public Mono<ResponseEntity<ClassVoyageDTO>> getClassVoyageById(@PathVariable UUID id) {
        return voyageUseCase.getClassVoyageById(id)
                .map(ResponseEntity::ok)
                .onErrorResume(ResourceNotFoundException.class, e -> Mono.just(ResponseEntity.notFound().build()));
    }

    @Operation(summary = "Créer une classe de voyage",
            description = "Ajoute une nouvelle classe de voyage à la base de données.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Classe de voyage créée avec succès",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ClassVoyageDTO.class))),
            @ApiResponse(responseCode = "400", description = "Données invalides")
    })
    @PostMapping
    public Mono<ResponseEntity<ClassVoyageDTO>> createClassVoyage(@RequestBody ClassVoyageDTO classVoyageDTO) {
        return voyageUseCase.createClassVoyage(classVoyageDTO)
                .map(created -> new ResponseEntity<>(created, HttpStatus.CREATED))
                .onErrorResume(Exception.class, e -> Mono.just(ResponseEntity.badRequest().build()));
    }

    @Operation(summary = "Mettre à jour une classe de voyage",
            description = "Modifie une classe de voyage existante.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Classe de voyage mise à jour avec succès",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ClassVoyageDTO.class))),
            @ApiResponse(responseCode = "404", description = "Classe de voyage non trouvée"),
            @ApiResponse(responseCode = "400", description = "Données invalides")
    })
    @PutMapping("/{id}")
    public Mono<ResponseEntity<ClassVoyageDTO>> updateClassVoyage(
            @PathVariable UUID id,
            @RequestBody ClassVoyageDTO classVoyageDTO) {

        return voyageUseCase.updateClassVoyage(id, classVoyageDTO)
                .map(ResponseEntity::ok)
                .onErrorResume(ResourceNotFoundException.class, e -> Mono.just(ResponseEntity.notFound().build()))
                .onErrorResume(Exception.class, e -> Mono.just(ResponseEntity.badRequest().build()));
    }

    @Operation(summary = "Supprimer une classe de voyage",
            description = "Supprime une classe de voyage en fonction de son identifiant.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Classe de voyage supprimée avec succès"),
            @ApiResponse(responseCode = "404", description = "Classe de voyage non trouvée")
    })
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteClassVoyage(@PathVariable UUID id) {
        return voyageUseCase.deleteClassVoyage(id)
                .then(Mono.just(new ResponseEntity<Void>(HttpStatus.NO_CONTENT)))
                .onErrorResume(ResourceNotFoundException.class, e -> Mono.just(ResponseEntity.notFound().build()));
    }
}
