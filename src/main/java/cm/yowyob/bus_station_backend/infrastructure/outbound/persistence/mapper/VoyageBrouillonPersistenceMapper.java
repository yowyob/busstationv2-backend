package cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.mapper;

import cm.yowyob.bus_station_backend.domain.enums.StatutBrouillon;
import cm.yowyob.bus_station_backend.domain.model.VoyageBrouillon;
import cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.entity.VoyageBrouillonEntity;
import org.springframework.stereotype.Component;

@Component
public class VoyageBrouillonPersistenceMapper {

    public VoyageBrouillon toDomain(VoyageBrouillonEntity e) {
        if (e == null) return null;
        return VoyageBrouillon.builder()
                .id(e.getId())
                .agenceVoyageId(e.getAgenceVoyageId())
                .ligneServiceId(e.getLigneServiceId())
                .titre(e.getTitre())
                .description(e.getDescription())
                .lieuDepart(e.getLieuDepart())
                .lieuArrive(e.getLieuArrive())
                .pointDeDepart(e.getPointDeDepart())
                .pointArrivee(e.getPointArrivee())
                .dateDepartPrev(e.getDateDepartPrev())
                .heureDepartEffectif(e.getHeureDepartEffectif())
                .heureArrive(e.getHeureArrive())
                .dureeEstimee(e.getDureeEstimee())
                .classVoyageId(e.getClassVoyageId())
                .vehiculeId(e.getVehiculeId())
                .chauffeurId(e.getChauffeurId())
                .nbrPlaceReservable(e.getNbrPlaceReservable())
                .prix(e.getPrix())
                .amenities(e.getAmenities())
                .smallImage(e.getSmallImage())
                .bigImage(e.getBigImage())
                .dateLimiteReservation(e.getDateLimiteReservation())
                .dateLimiteConfirmation(e.getDateLimiteConfirmation())
                .statutBrouillon(e.getStatutBrouillon() != null ? StatutBrouillon.valueOf(e.getStatutBrouillon()) : null)
                .notes(e.getNotes())
                .voyageId(e.getVoyageId())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }

    public VoyageBrouillonEntity toEntity(VoyageBrouillon d) {
        if (d == null) return null;
        return VoyageBrouillonEntity.builder()
                .id(d.getId())
                .agenceVoyageId(d.getAgenceVoyageId())
                .ligneServiceId(d.getLigneServiceId())
                .titre(d.getTitre())
                .description(d.getDescription())
                .lieuDepart(d.getLieuDepart())
                .lieuArrive(d.getLieuArrive())
                .pointDeDepart(d.getPointDeDepart())
                .pointArrivee(d.getPointArrivee())
                .dateDepartPrev(d.getDateDepartPrev())
                .heureDepartEffectif(d.getHeureDepartEffectif())
                .heureArrive(d.getHeureArrive())
                .dureeEstimee(d.getDureeEstimee())
                .classVoyageId(d.getClassVoyageId())
                .vehiculeId(d.getVehiculeId())
                .chauffeurId(d.getChauffeurId())
                .nbrPlaceReservable(d.getNbrPlaceReservable())
                .prix(d.getPrix())
                .amenities(d.getAmenities())
                .smallImage(d.getSmallImage())
                .bigImage(d.getBigImage())
                .dateLimiteReservation(d.getDateLimiteReservation())
                .dateLimiteConfirmation(d.getDateLimiteConfirmation())
                .statutBrouillon(d.getStatutBrouillon() != null ? d.getStatutBrouillon().name() : null)
                .notes(d.getNotes())
                .voyageId(d.getVoyageId())
                .createdAt(d.getCreatedAt())
                .updatedAt(d.getUpdatedAt())
                .build();
    }
}