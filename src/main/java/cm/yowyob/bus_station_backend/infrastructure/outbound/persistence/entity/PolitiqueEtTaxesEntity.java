package cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.entity;

import cm.yowyob.bus_station_backend.domain.enums.PolitiqueOuTaxe;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;
import java.util.UUID;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table("politique_et_taxes")
public class PolitiqueEtTaxesEntity implements Persistable<UUID> {

    @Id
    @Column("id_politique")
    private UUID idPolitique;

    @Column("gare_routiere_id")
    private UUID gareRoutiereId;

    @Column("nom_politique")
    private String nomPolitique;

    @Column("description")
    private String description;

    @Column("taux_taxe")
    private Double tauxTaxe;

    @Column("montant_fixe")
    private Double montantFixe;

    @Column("date_effet")
    private LocalDate dateEffet;

    @Column("document_url")
    private String documentUrl;

    @Column("type")
    private PolitiqueOuTaxe type;


    @Transient
    private boolean isNew;

    @Override
    public UUID getId() {
        return this.idPolitique;
    }

    @Override
    public boolean isNew() {
        return this.isNew || this.idPolitique == null;
    }

    public void setAsNew() {
        this.isNew = true;
    }

}
