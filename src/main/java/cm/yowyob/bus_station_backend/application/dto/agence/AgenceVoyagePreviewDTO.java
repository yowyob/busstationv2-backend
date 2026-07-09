package cm.yowyob.bus_station_backend.application.dto.agence;

import java.util.UUID;

import lombok.Data;

@Data
public class AgenceVoyagePreviewDTO {
  private UUID idAgenceVoyage;
  private String longName;
  private String shortName;
  private String location;
}
