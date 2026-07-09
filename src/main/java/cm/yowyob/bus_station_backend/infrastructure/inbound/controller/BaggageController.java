package cm.yowyob.bus_station_backend.infrastructure.inbound.controller;

import cm.yowyob.bus_station_backend.application.dto.reservation.BaggageDTO;
import cm.yowyob.bus_station_backend.application.port.in.ReservationUseCase;
import cm.yowyob.bus_station_backend.domain.exception.ResourceNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/baggages")
@RequiredArgsConstructor
public class BaggageController {

    private final ReservationUseCase reservationUseCase;

    @Operation(summary = "Obtenir tous les bagages", description = "Récupère la liste de tous les bagages.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Liste récupérée avec succès",
                    content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = BaggageDTO.class))))
    })
    @GetMapping
    public Mono<ResponseEntity<List<BaggageDTO>>> getAllBaggages() {
        // On collecte le Flux en List pour maintenir la compatibilité JSON (JSON Array)
        return reservationUseCase.getAllBagagesByReservation()
                .collectList()
                .map(ResponseEntity::ok);
    }

    @Operation(summary = "Obtenir un bagage par ID", description = "Récupère un bagage spécifique par son ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Bagage trouvé", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BaggageDTO.class))),
            @ApiResponse(responseCode = "404", description = "Bagage non trouvé")
    })
    @GetMapping("/{id}")
    public Mono<ResponseEntity<BaggageDTO>> getBaggageById(@PathVariable UUID id) {
        return reservationUseCase.getBaggageById(id)
                .map(ResponseEntity::ok)
                .onErrorResume(ResourceNotFoundException.class, e -> Mono.just(ResponseEntity.notFound().build()));
    }

    @Operation(summary = "Créer un bagage", description = "Ajoute un nouveau bagage.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Bagage créé avec succès", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BaggageDTO.class))),
            @ApiResponse(responseCode = "400", description = "Données invalides")
    })
    @PostMapping
    public Mono<ResponseEntity<BaggageDTO>> createBaggage(@RequestBody BaggageDTO baggageDTO) {
        return reservationUseCase.createBaggage(baggageDTO)
                .map(created -> new ResponseEntity<>(created, HttpStatus.CREATED))
                .onErrorResume(Exception.class, e -> Mono.just(ResponseEntity.badRequest().build()));
    }

    @Operation(summary = "Mettre à jour un bagage", description = "Modifie un bagage existant.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Bagage mis à jour avec succès", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BaggageDTO.class))),
            @ApiResponse(responseCode = "404", description = "Bagage non trouvé"),
            @ApiResponse(responseCode = "400", description = "Données invalides")
    })
    @PutMapping("/{id}")
    public Mono<ResponseEntity<BaggageDTO>> updateBaggage(@PathVariable UUID id, @RequestBody BaggageDTO baggageDTO) {
        return reservationUseCase.updateBaggage(id, baggageDTO)
                .map(ResponseEntity::ok)
                .onErrorResume(ResourceNotFoundException.class, e -> Mono.just(ResponseEntity.notFound().build()))
                .onErrorResume(Exception.class, e -> Mono.just(ResponseEntity.badRequest().build()));
    }

    @Operation(summary = "Supprimer un bagage", description = "Supprime un bagage par son ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Bagage supprimé avec succès"),
            @ApiResponse(responseCode = "404", description = "Bagage non trouvé")
    })
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteBaggage(@PathVariable UUID id) {
        return reservationUseCase.deleteBaggage(id)
                .then(Mono.just(new ResponseEntity<Void>(HttpStatus.NO_CONTENT)))
                .onErrorResume(ResourceNotFoundException.class, e -> Mono.just(ResponseEntity.notFound().build()));
    }
}
