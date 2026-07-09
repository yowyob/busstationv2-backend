package cm.yowyob.bus_station_backend.application.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO partagé pour les webhooks /paiement/confirmer et /paiement/echec.
 * - confirmer : transactionCode + reservationId + montantPaye (optionnel, défaut prixTotal)
 * - echec    : transactionCode + reservationId + motif (optionnel)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaiementCallbackDTO {
    private String transactionCode;
    private UUID reservationId;
    private Double montantPaye;
    private String motif;
}