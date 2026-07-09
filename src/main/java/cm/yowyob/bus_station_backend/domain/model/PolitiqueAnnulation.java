package cm.yowyob.bus_station_backend.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PolitiqueAnnulation {
    private UUID idPolitique;
    private List<TauxPeriode> listeTauxPeriode;
    private Duration dureeCoupon;
    private UUID idAgenceVoyage;

    public double calculerTauxRemboursement(ClassVoyage classVoyage, LocalDateTime dateLimReservation, LocalDateTime dateLimConfirmation, LocalDateTime now) {
        if (dateLimReservation == null || dateLimConfirmation == null || now == null) return 0.0;

        long dateLimReservattionLong = dateLimReservation.toEpochSecond(ZoneOffset.UTC);
        long dateLimConfirmationLong = dateLimConfirmation.toEpochSecond(ZoneOffset.UTC);
        long nowLong = now.toEpochSecond(ZoneOffset.UTC);

        double range = (double) (dateLimConfirmationLong - dateLimReservattionLong);
        double tauxDateAnnulation = range == 0 ? 1.0 : (double) (nowLong - dateLimReservattionLong) / range;
        
        double tauxClassVoyage = 1.0;
        double tauxPolitique = 1.0;

        if (this.getListeTauxPeriode() != null) {
            for (TauxPeriode tp : this.getListeTauxPeriode()) {
                if (tp.getDateDebut() != null && tp.getDateFin() != null) {
                    if (now.isAfter(tp.getDateDebut()) && now.isBefore(tp.getDateFin())) {
                        tauxPolitique = tp.getValeur();
                        break;
                    }
                }
            }
        }

        if (classVoyage != null) {
            tauxClassVoyage = classVoyage.getTauxAnnulation();
        }

        return (tauxDateAnnulation + tauxClassVoyage + tauxPolitique) / 3.0;
    }
}
