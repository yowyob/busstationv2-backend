package cm.yowyob.bus_station_backend.domain.util;

import cm.yowyob.bus_station_backend.domain.model.ClassVoyage;
import cm.yowyob.bus_station_backend.domain.model.PolitiqueAnnulation;
import cm.yowyob.bus_station_backend.domain.model.TauxPeriode;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class AnnulationOperator {

    public static double tauxannulation(ClassVoyage classVoyage, PolitiqueAnnulation politiqueAnnulation,
                                        LocalDateTime dateLimReservation, LocalDateTime dateLimConfirmation, LocalDateTime now) {
        if (dateLimReservation == null || dateLimConfirmation == null || now == null) return 0.0;
        
        long dateLimReservattionLong = dateLimReservation.toEpochSecond(ZoneOffset.UTC);
        long dateLimConfirmationLong = dateLimConfirmation.toEpochSecond(ZoneOffset.UTC);
        long nowLong = now.toEpochSecond(ZoneOffset.UTC);
        
        double range = (double) (dateLimConfirmationLong - dateLimReservattionLong);
        double tauxDateAnnulation = range == 0 ? 1.0 : (double) (nowLong - dateLimReservattionLong) / range;
        
        double tauxClassVoyage = 1.0;
        double tauxPolitique = 1.0;
        if (politiqueAnnulation != null && politiqueAnnulation.getListeTauxPeriode() != null) {
            for (TauxPeriode tp : politiqueAnnulation.getListeTauxPeriode()) {
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

    public static double tauxCompensation(ClassVoyage classVoyage, PolitiqueAnnulation politiqueAnnulation,
                                          LocalDateTime dateLimReservation, LocalDateTime dateLimConfirmation, LocalDateTime now) {
        return tauxannulation(classVoyage, politiqueAnnulation, dateLimReservation, dateLimConfirmation, now);
    }
}
