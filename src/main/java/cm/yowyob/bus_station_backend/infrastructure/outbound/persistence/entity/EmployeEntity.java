package cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.entity;

import cm.yowyob.bus_station_backend.domain.enums.StatutEmploye;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Table("employes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeEntity implements Persistable<UUID> {
    @Id
    @Column("id")
    private UUID employeId;
    @Column("agence_id")
    private UUID agenceVoyageId;
    @Column("user_id")
    private UUID userId;
    @Column("poste")
    private String poste;
    @Column("date_embauche")
    private LocalDateTime dateEmbauche;
    @Column("date_fin_contrat")
    private LocalDateTime dateFinContrat;
    @Column("statut_employe")
    private StatutEmploye statutEmploye;
    @Column("salaire")
    private Double salaire;
    @Column("departement")
    private String departement;
    @Column("manager_id")
    private UUID managerId;

    @Transient
    private boolean isNew = false;

    @Override
    public UUID getId() { return employeId; }

    @Override
    public boolean isNew() { return isNew || employeId == null; }

    public void setAsNew() { this.isNew = true; }
}
