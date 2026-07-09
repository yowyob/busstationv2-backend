package cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.mapper;

import cm.yowyob.bus_station_backend.domain.enums.Amenities;
import cm.yowyob.bus_station_backend.domain.model.Voyage;
import cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.entity.VoyageEntity;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class VoyagePersistenceMapper {

    public Voyage toDomain(VoyageEntity entity) {
        if (entity == null)
            return null;

        return Voyage.builder()
                .idVoyage(entity.getIdVoyage())
                .titre(entity.getTitre())
                .description(entity.getDescription())
                .dateDepartPrev(entity.getDateDepartPrev())
                .lieuDepart(entity.getLieuDepart())
                .dateDepartEffectif(entity.getDateDepartEffectif())
                .dateArriveEffectif(entity.getDateArriveEffectif())
                .lieuArrive(entity.getLieuArrive())
                .heureDepartEffectif(entity.getHeureDepartEffectif())
                .pointDeDepart(entity.getPointDeDepart())
                .pointArrivee(entity.getPointArrivee())
                .dureeVoyage(entity.getDureeVoyage() != null ? Duration.ofSeconds(entity.getDureeVoyage()) : null)
                .heureArrive(entity.getHeureArrive())
                .nbrPlaceReservable(entity.getNbrPlaceReservable())
                .nbrPlaceReserve(entity.getNbrPlaceReserve())
                .nbrPlaceConfirm(entity.getNbrPlaceConfirm())
                .nbrPlaceRestante(entity.getNbrPlaceRestante())
                .datePublication(entity.getDatePublication())
                .dateLimiteReservation(entity.getDateLimiteReservation())
                .dateLimiteConfirmation(entity.getDateLimiteConfirmation())
                .statusVoyage(entity.getStatusVoyage())
                .smallImage(entity.getSmallImage())
                .bigImage(entity.getBigImage())
                .amenities(entity.getAmenities())
                .build();
    }

    public VoyageEntity toEntity(Voyage domain) {
        if (domain == null)
            return null;

        return VoyageEntity.builder()
                .idVoyage(domain.getIdVoyage())
                .titre(domain.getTitre())
                .description(domain.getDescription())
                .dateDepartPrev(domain.getDateDepartPrev())
                .lieuDepart(domain.getLieuDepart())
                .dateDepartEffectif(domain.getDateDepartEffectif())
                .dateArriveEffectif(domain.getDateArriveEffectif())
                .lieuArrive(domain.getLieuArrive())
                .heureDepartEffectif(domain.getHeureDepartEffectif())
                .pointDeDepart(domain.getPointDeDepart())
                .pointArrivee(domain.getPointArrivee())
                .dureeVoyage(domain.getDureeVoyage() != null ? domain.getDureeVoyage().getSeconds() : null)
                .heureArrive(domain.getHeureArrive())
                .nbrPlaceReservable(domain.getNbrPlaceReservable())
                .nbrPlaceReserve(domain.getNbrPlaceReserve())
                .nbrPlaceConfirm(domain.getNbrPlaceConfirm())
                .nbrPlaceRestante(domain.getNbrPlaceRestante())
                .datePublication(domain.getDatePublication())
                .dateLimiteReservation(domain.getDateLimiteReservation())
                .dateLimiteConfirmation(domain.getDateLimiteConfirmation())
                .statusVoyage(domain.getStatusVoyage())
                .smallImage(domain.getSmallImage())
                .bigImage(domain.getBigImage())
                .amenities(formatAmenities(domain.getAmenities()))
                .build();
    }

    private List<Amenities> parseAmenities(String amenities) {
        if (amenities == null || amenities.isBlank())
            return List.of();
        return Arrays.stream(amenities.split(","))
                .map(Amenities::valueOf)
                .toList();
    }

    private String formatAmenities(List<Amenities> amenities) {
        if (amenities == null || amenities.isEmpty())
            return "";
        return amenities.stream()
                .map(Enum::name)
                .collect(Collectors.joining(","));
    }
}
