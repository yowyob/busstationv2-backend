package cm.yowyob.bus_station_backend.application.dto.gareRoutiere;

import cm.yowyob.bus_station_backend.domain.enums.ServicesGareRoutiere;
import java.util.List;
import java.util.UUID;

import lombok.Data;

@Data
public class GareRoutierePreviewDTO {
  private UUID idGareRoutiere;
  private String nomGareRoutiere;
  private String ville;
  private String quartier;
  private String photoUrl;
  private List<ServicesGareRoutiere> services;
  private Integer nbreAgence;
  private boolean isOpen;
}
