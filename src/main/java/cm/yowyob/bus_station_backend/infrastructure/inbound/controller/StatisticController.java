package cm.yowyob.bus_station_backend.infrastructure.inbound.controller;

import cm.yowyob.bus_station_backend.application.dto.statistic.AgenceEvolutionDTO;
import cm.yowyob.bus_station_backend.application.dto.statistic.AgenceStatisticsDTO;
import cm.yowyob.bus_station_backend.application.port.in.ReportingUseCase;
import cm.yowyob.bus_station_backend.domain.exception.ResourceNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.UUID;

@RestController
@AllArgsConstructor
@RequestMapping("/statistiques")
@SecurityRequirement(name = "bearerAuth")
@Slf4j
public class StatisticController {

    private final ReportingUseCase reportingUseCase;

    @Operation(summary = "Obtenir les statistiques générales d'une agence",
            description = "Récupère toutes les statistiques chiffrées d'une agence : nombre d'employés, voyages, réservations, revenus, etc.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Statistiques récupérées avec succès",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AgenceStatisticsDTO.class))),
            @ApiResponse(responseCode = "404", description = "Agence non trouvée"),
            @ApiResponse(responseCode = "500", description = "Erreur interne du serveur")
    })
    @GetMapping("/agence/{agenceId}/general")
    public Mono<ResponseEntity<Object>> getAgenceStatistics(@PathVariable UUID agenceId) {
        log.info("Récupération des statistiques générales pour l'agence {}", agenceId);

        return reportingUseCase.getAgenceStatistics(agenceId)
                .map(stats -> ResponseEntity.ok((Object) stats))
                .onErrorResume(ResourceNotFoundException.class, e -> {
                    log.error("Agence non trouvée {}: {}", agenceId, e.getMessage());
                    return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage()));
                })
                .onErrorResume(Exception.class, e -> {
                    log.error("Erreur interne pour l'agence {}: {}", agenceId, e.getMessage());
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body("Erreur lors de la récupération des statistiques"));
                });
    }

    @Operation(summary = "Obtenir les évolutions dans le temps pour une agence",
            description = "Récupère les données d'évolution temporelle : réservations, voyages, revenus et utilisateurs sur les derniers mois.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Évolutions récupérées avec succès",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AgenceEvolutionDTO.class))),
            @ApiResponse(responseCode = "404", description = "Agence non trouvée"),
            @ApiResponse(responseCode = "500", description = "Erreur interne du serveur")
    })
    @GetMapping("/agence/{agenceId}/evolution")
    public Mono<ResponseEntity<Object>> getAgenceEvolution(@PathVariable UUID agenceId) {
        log.info("Récupération des évolutions temporelles pour l'agence {}", agenceId);

        return reportingUseCase.getAgenceEvolution(agenceId)
                .map(evolution -> ResponseEntity.ok((Object) evolution))
                .onErrorResume(ResourceNotFoundException.class, e ->
                        Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage())))
                .onErrorResume(Exception.class, e ->
                        Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body("Erreur lors de la récupération des évolutions")));
    }

    @Operation(summary = "Obtenir toutes les statistiques d'une agence",
            description = "Récupère à la fois les statistiques générales et les évolutions temporelles en un seul appel.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Toutes les statistiques récupérées avec succès"),
            @ApiResponse(responseCode = "404", description = "Agence non trouvée"),
            @ApiResponse(responseCode = "500", description = "Erreur interne du serveur")
    })
    @GetMapping("/agence/{agenceId}/complete")
    public Mono<ResponseEntity<Object>> getCompleteAgenceStatistics(@PathVariable UUID agenceId) {
        log.info("Récupération des statistiques complètes pour l'agence {}", agenceId);

        // Mono.zip permet d'exécuter les deux appels en parallèle de manière réactive
        return Mono.zip(
                        reportingUseCase.getAgenceStatistics(agenceId),
                        reportingUseCase.getAgenceEvolution(agenceId)
                ).map(tuple -> {
                    // Recréation de la structure de l'objet anonyme utilisé dans l'ancien backend
                    // { "general": stats, "evolution": evolution }
                    Map<String, Object> completeStats = Map.of(
                            "general", tuple.getT1(),
                            "evolution", tuple.getT2()
                    );
                    return ResponseEntity.ok((Object) completeStats);
                })
                .onErrorResume(ResourceNotFoundException.class, e ->
                        Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage())))
                .onErrorResume(Exception.class, e -> {
                    log.error("Erreur stats complètes pour l'agence {}: {}", agenceId, e.getMessage());
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body("Erreur lors de la récupération des statistiques complètes"));
                });
    }
}