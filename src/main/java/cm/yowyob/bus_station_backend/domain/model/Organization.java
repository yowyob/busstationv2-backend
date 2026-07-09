package cm.yowyob.bus_station_backend.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Organization {
    private UUID id;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
    private UUID createdBy;
    private UUID updatedBy;
    private UUID organizationId;
    private List<UUID> businessDomains;
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
    private List<String> keywords;
    private String status;
}
