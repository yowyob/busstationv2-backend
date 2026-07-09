package cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Table("organization")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationEntity implements Persistable<UUID>{
    @Id
    private UUID id;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
    private UUID createdBy;
    private UUID updatedBy;
    private UUID organizationId;
    private UUID[] businessDomains;
    private String email;
    private String shortName;
    private String longName;
    private String description;
    private String logoUrl;
    private boolean isIndividualBusiness;
    private String legalForm;
    private boolean isActive;
    private String websiteUrl;
    private String socialNetwork;
    private String businessRegistrationNumber;
    private String taxNumber;
    private Double capitalShare;
    private LocalDateTime registrationDate;
    private String ceoName;
    private LocalDateTime yearFounded;
    private String[] keywords;
    private String status;

    @Transient
    private boolean isNew = false;

    @Override
    public UUID getId() { return id; }

    @Override
    public boolean isNew() { return isNew || id == null; }

    public void setAsNew() { this.isNew = true; }
}
