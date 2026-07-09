package cm.yowyob.bus_station_backend.domain.model;

import java.util.List;
import java.util.UUID;

import cm.yowyob.bus_station_backend.domain.enums.ServicesGareRoutiere;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GareRoutiere {
  private UUID idGareRoutiere;
  private String nomGareRoutiere;
  private String adresse;
  private String ville;
  private String quartier;
  private String description;
  private String services;
  private String horaires;
  private String photoUrl;
  private String nomPresident;
  private Integer nbreAgence;

  private UUID idCoordonneeGPS;
  private UUID managerId;

  public void setServices(List<ServicesGareRoutiere> servicesList) {
    if (servicesList == null || servicesList.isEmpty()) {
      this.services = "";
      return;
    }
    this.services = servicesList.stream()
        .map(ServicesGareRoutiere::name)
        .reduce((a, b) -> a + "," + b)
        .orElse("");
  }

  public List<ServicesGareRoutiere> getServices() {
    if (services == null || services.isEmpty()) {
      return List.of();
    }
    return List.of(services.split(",")).stream()
        .map(ServicesGareRoutiere::valueOf)
        .toList();
  }
}
