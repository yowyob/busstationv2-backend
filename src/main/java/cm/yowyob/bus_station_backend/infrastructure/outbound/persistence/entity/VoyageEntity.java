package cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.entity;

import cm.yowyob.bus_station_backend.domain.enums.StatutVoyage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Table("voyages")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoyageEntity implements Persistable<UUID> {
    @Id
    private UUID idVoyage;
    private String titre;
    private String description;
    private LocalDateTime dateDepartPrev;
    private String lieuDepart;
    private LocalDateTime dateDepartEffectif;
    private LocalDateTime dateArriveEffectif;
    private String lieuArrive;
    private LocalDateTime heureDepartEffectif;
    private String pointDeDepart;
    private String pointArrivee;
    private Long dureeVoyage;
    private LocalDateTime heureArrive;
    private int nbrPlaceReservable;
    private int nbrPlaceReserve;
    private int nbrPlaceConfirm;
    private int nbrPlaceRestante;
    private LocalDateTime datePublication;
    private LocalDateTime dateLimiteReservation;
    private LocalDateTime dateLimiteConfirmation;
    private StatutVoyage statusVoyage;
    private String smallImage;
    private String bigImage;

    private String amenities;

    @Transient
    private boolean isNew = false;

    @Override
    public UUID getId() {
        return idVoyage;
    }

    @Override
    public boolean isNew() {
        return isNew || idVoyage == null;
    }

    public void setAsNew() {
        this.isNew = true;
    }
}
