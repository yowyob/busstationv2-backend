package cm.yowyob.bus_station_backend.infrastructure.inbound.controller;

import cm.yowyob.bus_station_backend.application.dto.organization.CreateOrganizationRequest;
import cm.yowyob.bus_station_backend.application.dto.organization.OrganizationDTO;
import cm.yowyob.bus_station_backend.application.port.in.OrganizationUseCase;
import cm.yowyob.bus_station_backend.domain.model.AgenceVoyage;
import static cm.yowyob.bus_station_backend.infrastructure.util.SecurityUtils.getCurrentUserId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import cm.yowyob.bus_station_backend.application.dto.organization.OrganizationDTO;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/organizations")
@RequiredArgsConstructor
public class OrganizationController {

    private final OrganizationUseCase organizationUseCase;

    @Operation(summary = "Obtenir toutes les agences d'une organisations",
            description = "Récupère la liste de toutes les agences de l'organisation")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Liste récupérée avec succès",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = AgenceVoyage.class))))
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/agencies/{organisationId}")
    public Mono<ResponseEntity<List<AgenceVoyage>>> getAllAgencies(@PathVariable UUID organisationId) {
        return organizationUseCase.findAllAgenciesByOrganization(organisationId)
                .collectList() // On collecte le Flux en List pour maintenir le format de réponse attendu par le frontend
                .map(agencies -> new ResponseEntity<>(agencies, HttpStatus.OK));
    }

    @Operation(summary = "Créer une nouvelle organisation",
            description = "Cette méthode permet d'enregistrer une organisation dans le système")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Organisation créée avec succès",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = OrganizationDTO.class))),
            @ApiResponse(responseCode = "400", description = "Données d'entrée invalides")
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/user/{userId}")
    public Mono<ResponseEntity<List<OrganizationDTO>>> getOrganizationsByUser(@PathVariable UUID userId) {
        return organizationUseCase.getOrganizationsByUser(userId)
                .collectList()
                .map(ResponseEntity::ok);
    }

    @PostMapping
    public Mono<ResponseEntity<OrganizationDTO>> createOrganization(@Valid @RequestBody CreateOrganizationRequest request) {
        return getCurrentUserId()
                .flatMap(userId -> {
                    request.setCreatedBy(userId);
                    return organizationUseCase.createOrganization(request);
                })
                .map(createdOrganization -> ResponseEntity.status(HttpStatus.CREATED).body(createdOrganization));
    }

@Operation(summary = "Obtenir une organisation par son ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Organisation trouvée",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = OrganizationDTO.class))),
            @ApiResponse(responseCode = "404", description = "Organisation non trouvée")
    })
    @GetMapping("/{organizationId}")
    public Mono<ResponseEntity<OrganizationDTO>> getOrganizationById(@PathVariable UUID organizationId) {
        return organizationUseCase.getOrganizationById(organizationId)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Mettre à jour une organisation")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Organisation mise à jour",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = OrganizationDTO.class))),
            @ApiResponse(responseCode = "404", description = "Organisation non trouvée")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/{organizationId}")
    public Mono<ResponseEntity<OrganizationDTO>> updateOrganization(
            @PathVariable UUID organizationId,
            @Valid @RequestBody OrganizationDTO organizationDTO) {
        return organizationUseCase.updateOrganization(organizationId, organizationDTO)
                .map(ResponseEntity::ok);
    }
}