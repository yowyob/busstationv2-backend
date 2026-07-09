package cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.mapper;

import cm.yowyob.bus_station_backend.domain.model.CreneauPlanning;
import cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.entity.CreneauPlanningEntity;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.Duration;

@Component
public class CreneauPlanningPersistenceMapper {

    public CreneauPlanningEntity toEntity(CreneauPlanning domain) {
        return CreneauPlanningEntity.builder()
                .idCreneau(domain.getIdCreneau())
                .idPlanning(domain.getIdPlanning())
                .jourSemaine(domain.getJourSemaine() != null ? domain.getJourSemaine().name() : null)
                .jourMois(domain.getJourMois())
                .mois(domain.getMois())
                .titre(domain.getTitre())
                .description(domain.getDescription())
                .heureDepart(domain.getHeureDepart())
                .heureArrivee(domain.getHeureArrivee())
                .dureeEstimeeMinutes(domain.getDureeEstimee() != null ? domain.getDureeEstimee().toMinutes() : null)
                .lieuDepart(domain.getLieuDepart())
                .lieuArrive(domain.getLieuArrive())
                .pointDeDepart(domain.getPointDeDepart())
                .pointArrivee(domain.getPointArrivee())
                .idClassVoyage(domain.getIdClassVoyage())
                .idVehicule(domain.getIdVehicule())
                .idChauffeur(domain.getIdChauffeur())
                .nbrPlacesDisponibles(domain.getNbrPlacesDisponibles())
                .delaiReservationHeures(domain.getDelaiReservationHeures())
                .delaiConfirmationHeures(domain.getDelaiConfirmationHeures())
                .smallImage(domain.getSmallImage())
                .bigImage(domain.getBigImage())
                .amenities(domain.getAmenities())
                .actif(domain.isActif())
                .build();
    }

    public CreneauPlanningEntity toNewEntity(CreneauPlanning domain) {
        CreneauPlanningEntity entity = toEntity(domain);
        entity.setAsNew();
        return entity;
    }

    public CreneauPlanning toDomain(CreneauPlanningEntity entity) {
        return CreneauPlanning.builder()
                .idCreneau(entity.getIdCreneau())
                .idPlanning(entity.getIdPlanning())
                .jourSemaine(entity.getJourSemaine() != null ? DayOfWeek.valueOf(entity.getJourSemaine()) : null)
                .jourMois(entity.getJourMois())
                .mois(entity.getMois())
                .titre(entity.getTitre())
                .description(entity.getDescription())
                .heureDepart(entity.getHeureDepart())
                .heureArrivee(entity.getHeureArrivee())
                .dureeEstimee(entity.getDureeEstimeeMinutes() != null
                        ? Duration.ofMinutes(entity.getDureeEstimeeMinutes()) : null)
                .lieuDepart(entity.getLieuDepart())
                .lieuArrive(entity.getLieuArrive())
                .pointDeDepart(entity.getPointDeDepart())
                .pointArrivee(entity.getPointArrivee())
                .idClassVoyage(entity.getIdClassVoyage())
                .idVehicule(entity.getIdVehicule())
                .idChauffeur(entity.getIdChauffeur())
                .nbrPlacesDisponibles(entity.getNbrPlacesDisponibles())
                .delaiReservationHeures(entity.getDelaiReservationHeures())
                .delaiConfirmationHeures(entity.getDelaiConfirmationHeures())
                .smallImage(entity.getSmallImage())
                .bigImage(entity.getBigImage())
                .amenities(entity.getAmenities())
                .actif(entity.isActif())
                .build();
    }
}
