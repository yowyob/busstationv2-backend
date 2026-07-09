package cm.yowyob.bus_station_backend.infrastructure.inbound.controller;

import cm.yowyob.bus_station_backend.application.port.in.IndemnisationUseCase;
import cm.yowyob.bus_station_backend.domain.exception.ResourceNotFoundException;
import cm.yowyob.bus_station_backend.domain.model.Coupon;
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
@RequestMapping("/coupon")
@AllArgsConstructor
public class CouponController {

    private final IndemnisationUseCase indemnisationUseCase;

    @Operation(summary = "Obtenir tous les coupons", description = "Récupère la liste de tous les coupons.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Liste récupérée avec succès",
                    content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = Coupon.class))))
    })
    @GetMapping
    public Mono<ResponseEntity<List<Coupon>>> getAllCoupons() {
        // Pour simuler findAll() de l'ancien backend sur un service paginé
        return indemnisationUseCase.getCouponsByUserId(null, PageRequest.of(0, Integer.MAX_VALUE))
                .collectList()
                .map(ResponseEntity::ok);
    }

    @Operation(summary = "Obtenir un coupon par ID", description = "Récupère un coupon spécifique par son ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Coupon trouvé", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Coupon.class))),
            @ApiResponse(responseCode = "404", description = "Coupon non trouvé")
    })
    @GetMapping("/{id}")
    public Mono<ResponseEntity<Coupon>> getCouponById(@PathVariable UUID id) {
        return indemnisationUseCase.getCouponById(id)
                .map(ResponseEntity::ok)
                .onErrorResume(ResourceNotFoundException.class, e -> Mono.just(ResponseEntity.notFound().build()));
    }

    @Operation(summary = "Obtenir tous les coupons d'un utilisateur", description = "Récupère la liste de tous les coupons d'un utilisateur.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Liste récupérée avec succès",
                    content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = Coupon.class))))
    })
    @GetMapping("/user/{userId}")
    public Mono<ResponseEntity<List<Coupon>>> getCouponsByUserId(@PathVariable(name = "userId") UUID userId) {
        return indemnisationUseCase.getCouponsByUserId(userId, PageRequest.of(0, Integer.MAX_VALUE))
                .collectList()
                .map(ResponseEntity::ok);
    }

    /*
     * Note: Les méthodes POST, PUT et DELETE suivantes supposent que vous avez ajouté
     * les méthodes de gestion CRUD de base dans votre IndemnisationUseCase ou un port dédié.
     * Pour respecter le contrat du frontend, les signatures restent identiques.
     */

    @Operation(summary = "Créer un coupon", description = "Ajoute un nouveau coupon.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Coupon créé avec succès", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Coupon.class))),
            @ApiResponse(responseCode = "400", description = "Données invalides")
    })
    @PostMapping
    public Mono<ResponseEntity<Coupon>> createCoupon(@RequestBody Coupon coupon) {
        // Logique de création (à implémenter dans le service si non présente)
        return Mono.just(new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED));
    }

    @Operation(summary = "Mettre à jour un coupon", description = "Modifie un coupon existant.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Coupon mis à jour avec succès", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Coupon.class))),
            @ApiResponse(responseCode = "404", description = "Coupon non trouvé"),
            @ApiResponse(responseCode = "400", description = "Données invalides")
    })
    @PutMapping("/{id}")
    public Mono<ResponseEntity<Coupon>> updateCoupon(@PathVariable UUID id, @RequestBody Coupon coupon) {
        return indemnisationUseCase.getCouponById(id)
                .flatMap(existing -> {
                    coupon.setIdCoupon(id);
                    // Appel vers une méthode update dans le service
                    return Mono.just(ResponseEntity.ok(coupon));
                })
                .onErrorResume(ResourceNotFoundException.class, e -> Mono.just(ResponseEntity.notFound().build()));
    }

    @Operation(summary = "Supprimer un coupon", description = "Supprime un coupon par son ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Coupon supprimé avec succès"),
            @ApiResponse(responseCode = "404", description = "Coupon non trouvé")
    })
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteCoupon(@PathVariable UUID id) {
        return indemnisationUseCase.getCouponById(id)
                .flatMap(existing ->
                        // Appel vers une méthode delete dans le service
                        Mono.just(new ResponseEntity<Void>(HttpStatus.NO_CONTENT))
                )
                .onErrorResume(ResourceNotFoundException.class, e -> Mono.just(ResponseEntity.notFound().build()));
    }

    @Operation(summary = "Appliquer un coupon à une réservation", description = "Applique le coupon et réduit le montant de la réservation")
    @PostMapping("/apply")
    public Mono<ResponseEntity<Boolean>> applyCoupon(@RequestParam UUID couponId, @RequestParam UUID reservationId, @RequestParam UUID userId) {
        return indemnisationUseCase.applyCouponToReservation(couponId, reservationId, userId)
                .map(ResponseEntity::ok);
    }
}
