package cm.yowyob.bus_station_backend.application.dto.gareRoutiere;

import java.util.List;
import java.util.UUID;

import org.springframework.http.codec.multipart.FilePart;

import cm.yowyob.bus_station_backend.domain.enums.ServicesGareRoutiere;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GareRoutiereRequestDTO {

  @NotNull(message = "Le nom de la gare routière est obligatoire")
  @NotBlank(message = "Le nom de la gare routière ne peut pas être vide")
  private String nomGareRoutiere;

  private String adresse;

  @NotNull(message = "La ville est obligatoire")
  @NotBlank(message = "La ville ne peut pas être vide")
  private String ville;

  @NotNull(message = "le quartier est obligatoire")
  @NotBlank(message = "le quartier ne peut pas être vide")
  private String quartier;

  private String description;
  private List<ServicesGareRoutiere> services;
  private String horaires;
  private FilePart photo;

  @NotNull(message = "Le nom du président est obligatoire")
  @NotBlank(message = "Le nom du président ne peut pas être vide")
  private String nomPresident;

  private UUID idCoordonneeGPS;

  @NotNull(message = "L'identifiant du gestionnaire est obligatoire")
  private UUID managerId;
}
