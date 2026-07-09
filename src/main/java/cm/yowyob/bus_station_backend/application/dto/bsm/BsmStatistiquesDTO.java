// src/main/java/cm/yowyob/bus_station_backend/application/dto/bsm/BsmStatistiquesDTO.java

package cm.yowyob.bus_station_backend.application.dto.bsm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * KPIs renvoyés au dashboard BSM pour une gare donnée.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BsmStatistiquesDTO {
    private UUID gareId;

    /** Nombre d'agences affiliées (via agences_voyage.gare_routiere_id) */
    private Long nbAgencesAffiliees;

    /** Nombre d'agences affiliées avec is_active = true */
    private Long nbAgencesActives;

    /** Voyages dont la date_depart_prev tombe aujourd'hui */
    private Long nbVoyagesAujourdhui;

    /** Voyages PUBLIE à venir (date_depart_prev > now) */
    private Long nbVoyagesAVenir;

    /** Taux de remplissage moyen (0.0 -> 1.0) sur les voyages PUBLIE de la gare */
    private Double tauxRemplissageMoyen;
}