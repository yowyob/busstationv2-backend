package cm.yowyob.bus_station_backend.application.mapper;

import cm.yowyob.bus_station_backend.application.dto.planning.CreneauPlanningDTO;
import cm.yowyob.bus_station_backend.application.dto.planning.PlanningVoyageDTO;
import cm.yowyob.bus_station_backend.application.dto.planning.PlanningVoyagePreviewDTO;
import cm.yowyob.bus_station_backend.domain.model.AgenceVoyage;
import cm.yowyob.bus_station_backend.domain.model.CreneauPlanning;
import cm.yowyob.bus_station_backend.domain.model.PlanningVoyage;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PlanningMapper {

    // --- PlanningVoyage ---

    public PlanningVoyage toDomain(PlanningVoyageDTO dto) {
        return PlanningVoyage.builder()
                .idPlanning(dto.getIdPlanning())
                .idAgenceVoyage(dto.getIdAgenceVoyage())
                .nom(dto.getNom())
                .description(dto.getDescription())
                .recurrence(dto.getRecurrence())
                .statut(dto.getStatut())
                .dateDebut(dto.getDateDebut())
                .dateFin(dto.getDateFin())
                .dateCreation(dto.getDateCreation())
                .dateModification(dto.getDateModification())
                .build();
    }

    public PlanningVoyageDTO toDTO(PlanningVoyage planning) {
        return PlanningVoyageDTO.builder()
                .idPlanning(planning.getIdPlanning())
                .idAgenceVoyage(planning.getIdAgenceVoyage())
                .nom(planning.getNom())
                .description(planning.getDescription())
                .recurrence(planning.getRecurrence())
                .statut(planning.getStatut())
                .dateDebut(planning.getDateDebut())
                .dateFin(planning.getDateFin())
                .dateCreation(planning.getDateCreation())
                .dateModification(planning.getDateModification())
                .build();
    }

    public PlanningVoyageDTO toDTOWithCreneaux(PlanningVoyage planning, List<CreneauPlanning> creneaux) {
        PlanningVoyageDTO dto = toDTO(planning);
        dto.setCreneaux(creneaux.stream().map(this::toCreneauDTO).toList());
        return dto;
    }

    public PlanningVoyagePreviewDTO toPreviewDTO(PlanningVoyage planning, int nombreCreneaux) {
        return PlanningVoyagePreviewDTO.builder()
                .idPlanning(planning.getIdPlanning())
                .nom(planning.getNom())
                .description(planning.getDescription())
                .recurrence(planning.getRecurrence())
                .statut(planning.getStatut())
                .dateDebut(planning.getDateDebut())
                .dateFin(planning.getDateFin())
                .nombreCreneaux(nombreCreneaux)
                .build();
    }

    public PlanningVoyagePreviewDTO toPreviewDTOWithAgence(PlanningVoyage planning, int nombreCreneaux, AgenceVoyage agence) {
        PlanningVoyagePreviewDTO dto = toPreviewDTO(planning, nombreCreneaux);
        dto.setNomAgence(agence != null ? agence.getLongName() : null);
        return dto;
    }

    // --- CreneauPlanning ---

    public CreneauPlanning toCreneauDomain(CreneauPlanningDTO dto) {
        CreneauPlanning creneau = CreneauPlanning.builder()
                .idCreneau(dto.getIdCreneau())
                .idPlanning(dto.getIdPlanning())
                .jourSemaine(dto.getJourSemaine())
                .jourMois(dto.getJourMois())
                .mois(dto.getMois())
                .titre(dto.getTitre())
                .description(dto.getDescription())
                .heureDepart(dto.getHeureDepart())
                .heureArrivee(dto.getHeureArrivee())
                .dureeEstimee(dto.getDureeEstimee())
                .lieuDepart(dto.getLieuDepart())
                .lieuArrive(dto.getLieuArrive())
                .pointDeDepart(dto.getPointDeDepart())
                .pointArrivee(dto.getPointArrivee())
                .idClassVoyage(dto.getIdClassVoyage())
                .idVehicule(dto.getIdVehicule())
                .idChauffeur(dto.getIdChauffeur())
                .nbrPlacesDisponibles(dto.getNbrPlacesDisponibles())
                .delaiReservationHeures(dto.getDelaiReservationHeures())
                .delaiConfirmationHeures(dto.getDelaiConfirmationHeures())
                .smallImage(dto.getSmallImage())
                .bigImage(dto.getBigImage())
                .actif(dto.isActif())
                .build();
        if (dto.getAmenities() != null) {
            creneau.setAmenitiesList(dto.getAmenities());
        }
        return creneau;
    }

    public CreneauPlanningDTO toCreneauDTO(CreneauPlanning creneau) {
        return CreneauPlanningDTO.builder()
                .idCreneau(creneau.getIdCreneau())
                .idPlanning(creneau.getIdPlanning())
                .jourSemaine(creneau.getJourSemaine())
                .jourMois(creneau.getJourMois())
                .mois(creneau.getMois())
                .titre(creneau.getTitre())
                .description(creneau.getDescription())
                .heureDepart(creneau.getHeureDepart())
                .heureArrivee(creneau.getHeureArrivee())
                .dureeEstimee(creneau.getDureeEstimee())
                .lieuDepart(creneau.getLieuDepart())
                .lieuArrive(creneau.getLieuArrive())
                .pointDeDepart(creneau.getPointDeDepart())
                .pointArrivee(creneau.getPointArrivee())
                .idClassVoyage(creneau.getIdClassVoyage())
                .idVehicule(creneau.getIdVehicule())
                .idChauffeur(creneau.getIdChauffeur())
                .nbrPlacesDisponibles(creneau.getNbrPlacesDisponibles())
                .delaiReservationHeures(creneau.getDelaiReservationHeures())
                .delaiConfirmationHeures(creneau.getDelaiConfirmationHeures())
                .smallImage(creneau.getSmallImage())
                .bigImage(creneau.getBigImage())
                .amenities(creneau.getAmenitiesList())
                .actif(creneau.isActif())
                .build();
    }
}
