package cm.yowyob.bus_station_backend.domain.model;

import cm.yowyob.bus_station_backend.domain.enums.StatutPayment;
import cm.yowyob.bus_station_backend.domain.enums.StatutReservation;
import cm.yowyob.bus_station_backend.domain.exception.ReservationException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Reservation {
    private UUID idReservation;
    private LocalDateTime dateReservation;
    private LocalDateTime dateConfirmation;
    private int nbrPassager;
    private double prixTotal;
    private StatutReservation statutReservation;
    private UUID idUser;
    private UUID idVoyage;

    // Paiement
    private StatutPayment statutPayement;
    private String transactionCode;
    private double montantPaye;

    // Méthodes métier pour la logique interne
    public boolean estPayee() {
        return this.statutPayement == StatutPayment.PAID;
    }

    public boolean estConfirmable() {
        return this.statutPayement == StatutPayment.PAID
                && this.montantPaye >= this.prixTotal;
    }

    public void confirmer(double montantNouveauPaiement) {
        if (this.statutReservation == StatutReservation.ANNULER) {
            throw new ReservationException("Impossible de confirmer une réservation annulée");
        }
        this.montantPaye += montantNouveauPaiement;

        if (this.montantPaye >= this.prixTotal) {
            this.statutReservation = StatutReservation.CONFIRMER;
            this.dateConfirmation = LocalDateTime.now();
            this.statutPayement = StatutPayment.PAID;
        } else {
            // Paiement partiel
            this.statutPayement = StatutPayment.PENDING;
        }
    }

    public void annuler() {
        if (this.statutReservation == StatutReservation.ANNULER) {
            throw new ReservationException("Réservation déjà annulée");
        }
        this.statutReservation = StatutReservation.ANNULER;
    }

    public void initierPaiement(String transactionCode) {
        this.transactionCode = transactionCode;
        this.statutPayement = StatutPayment.PENDING;
    }

    public boolean estPayeTotalement() {
        return this.montantPaye >= this.prixTotal;
    }
}