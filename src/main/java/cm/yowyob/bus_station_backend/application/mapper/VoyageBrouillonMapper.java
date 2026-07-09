package cm.yowyob.bus_station_backend.application.mapper;

import cm.yowyob.bus_station_backend.application.dto.voyage.VoyageBrouillonCreateDTO;
import cm.yowyob.bus_station_backend.application.dto.voyage.VoyageBrouillonResponseDTO;
import cm.yowyob.bus_station_backend.application.dto.voyage.VoyageBrouillonUpdateDTO;
import cm.yowyob.bus_station_backend.domain.enums.StatutBrouillon;
import cm.yowyob.bus_station_backend.domain.model.VoyageBrouillon;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class VoyageBrouillonMapper {

    public VoyageBrouillon toDomain(VoyageBrouillonCreateDTO dto) {
        if (dto == null) return null;
        return VoyageBrouillon.builder()
                .agenceVoyageId(dto.getAgenceVoyageId())
                .ligneServiceId(dto.getLigneServiceId())
                .titre(dto.getTitre())
                .description(dto.getDescription())
                .lieuDepart(dto.getLieuDepart())
                .lieuArrive(dto.getLieuArrive())
                .pointDeDepart(dto.getPointDeDepart())
                .pointArrivee(dto.getPointArrivee())
                .dateDepartPrev(dto.getDateDepartPrev())
                .heureDepartEffectif(dto.getHeureDepartEffectif())
                .heureArrive(dto.getHeureArrive())
                .dureeEstimee(dto.getDureeEstimee())
                .classVoyageId(dto.getClassVoyageId())
                .vehiculeId(dto.getVehiculeId())
                .chauffeurId(dto.getChauffeurId())
                .nbrPlaceReservable(dto.getNbrPlaceReservable())
                .prix(dto.getPrix())
                .amenities(formatAmenities(dto.getAmenities()))
                .smallImage(dto.getSmallImage())
                .bigImage(dto.getBigImage())
                .dateLimiteReservation(dto.getDateLimiteReservation())
                .dateLimiteConfirmation(dto.getDateLimiteConfirmation())
                .notes(dto.getNotes())
                .build();
    }

    /**
     * Applique les champs non-null du DTO sur le domain existant (PATCH-like).
     */
    public void applyUpdate(VoyageBrouillon target, VoyageBrouillonUpdateDTO dto) {
        if (dto == null || target == null) return;
        if (dto.getLigneServiceId() != null) target.setLigneServiceId(dto.getLigneServiceId());
        if (dto.getTitre() != null) target.setTitre(dto.getTitre());
        if (dto.getDescription() != null) target.setDescription(dto.getDescription());
        if (dto.getLieuDepart() != null) target.setLieuDepart(dto.getLieuDepart());
        if (dto.getLieuArrive() != null) target.setLieuArrive(dto.getLieuArrive());
        if (dto.getPointDeDepart() != null) target.setPointDeDepart(dto.getPointDeDepart());
        if (dto.getPointArrivee() != null) target.setPointArrivee(dto.getPointArrivee());
        if (dto.getDateDepartPrev() != null) target.setDateDepartPrev(dto.getDateDepartPrev());
        if (dto.getHeureDepartEffectif() != null) target.setHeureDepartEffectif(dto.getHeureDepartEffectif());
        if (dto.getHeureArrive() != null) target.setHeureArrive(dto.getHeureArrive());
        if (dto.getDureeEstimee() != null) target.setDureeEstimee(dto.getDureeEstimee());
        if (dto.getClassVoyageId() != null) target.setClassVoyageId(dto.getClassVoyageId());
        if (dto.getVehiculeId() != null) target.setVehiculeId(dto.getVehiculeId());
        if (dto.getChauffeurId() != null) target.setChauffeurId(dto.getChauffeurId());
        if (dto.getNbrPlaceReservable() != null) target.setNbrPlaceReservable(dto.getNbrPlaceReservable());
        if (dto.getPrix() != null) target.setPrix(dto.getPrix());
        if (dto.getAmenities() != null) target.setAmenities(formatAmenities(dto.getAmenities()));
        if (dto.getSmallImage() != null) target.setSmallImage(dto.getSmallImage());
        if (dto.getBigImage() != null) target.setBigImage(dto.getBigImage());
        if (dto.getDateLimiteReservation() != null) target.setDateLimiteReservation(dto.getDateLimiteReservation());
        if (dto.getDateLimiteConfirmation() != null) target.setDateLimiteConfirmation(dto.getDateLimiteConfirmation());
        if (dto.getNotes() != null) target.setNotes(dto.getNotes());
    }

    public VoyageBrouillonResponseDTO toResponse(VoyageBrouillon domain) {
        if (domain == null) return null;
        return VoyageBrouillonResponseDTO.builder()
                .id(domain.getId())
                .agenceVoyageId(domain.getAgenceVoyageId())
                .ligneServiceId(domain.getLigneServiceId())
                .titre(domain.getTitre())
                .description(domain.getDescription())
                .lieuDepart(domain.getLieuDepart())
                .lieuArrive(domain.getLieuArrive())
                .pointDeDepart(domain.getPointDeDepart())
                .pointArrivee(domain.getPointArrivee())
                .dateDepartPrev(domain.getDateDepartPrev())
                .heureDepartEffectif(domain.getHeureDepartEffectif())
                .heureArrive(domain.getHeureArrive())
                .dureeEstimee(domain.getDureeEstimee())
                .classVoyageId(domain.getClassVoyageId())
                .vehiculeId(domain.getVehiculeId())
                .chauffeurId(domain.getChauffeurId())
                .nbrPlaceReservable(domain.getNbrPlaceReservable())
                .prix(domain.getPrix())
                .amenities(parseAmenities(domain.getAmenities()))
                .smallImage(domain.getSmallImage())
                .bigImage(domain.getBigImage())
                .dateLimiteReservation(domain.getDateLimiteReservation())
                .dateLimiteConfirmation(domain.getDateLimiteConfirmation())
                .statutBrouillon(domain.getStatutBrouillon())
                .notes(domain.getNotes())
                .voyageId(domain.getVoyageId())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }

    private static String formatAmenities(List<String> amenities) {
        if (amenities == null || amenities.isEmpty()) return null;
        return String.join(",", amenities);
    }

    private static List<String> parseAmenities(String csv) {
        if (csv == null || csv.isBlank()) return List.of();
        return Arrays.asList(csv.split(","));
    }
}