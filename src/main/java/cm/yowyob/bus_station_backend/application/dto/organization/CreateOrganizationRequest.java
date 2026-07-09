package cm.yowyob.bus_station_backend.application.dto.organization;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateOrganizationRequest {
    @NotBlank(message = "Long name is required")
    @JsonProperty("long_name")
    private String longName;

    @NotBlank(message = "Short name is required")
    @JsonProperty("short_name")
    private String shortName;

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    private String email;

    @Size(max = 500, message = "Description cannot be longer than 500 characters")
    private String description;

    @JsonProperty("business_domains")
    private List<UUID> businessDomains;

    @JsonProperty("created_by")
    private UUID createdBy;

    @JsonProperty("logo_url")
    private String logoUrl;

    // @NotBlank(message = "Legal form is required")
    @JsonProperty("legal_form")
    private String legalForm;

    @JsonProperty("web_site_url")
    private String websiteUrl;

    @JsonProperty("social_network")
    private String socialNetwork;

    @JsonProperty("business_registration_number")
    private String businessRegistrationNumber;

    @JsonProperty("tax_number")
    private String taxNumber;

    @JsonProperty("capital_share")
    private Double capitalShare;

    @JsonProperty("registration_date")
    private LocalDateTime registrationDate;

    @JsonProperty("ceo_name")
    private String ceoName;

    @JsonProperty("year_founded")
    private LocalDateTime yearFounded;

    private List<String> keywords;

    @JsonProperty("number_of_employees")
    private Integer numberOfEmployees;
}
