package cm.yowyob.bus_station_backend.application.dto.agence;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AgenceVoyageDTO {
    private UUID organisation_id;

    @NotNull(message = "L'ID du chef d'agence est requis")
    private UUID user_id;

    @NotBlank(message = "Le nom complet de l'agence est requis")
    @Size(min = 3, max = 100, message = "Le nom complet doit contenir entre 3 et 100 caractères")
    private String long_name;

    @NotBlank(message = "Le nom court de l'agence est requis")
    @Size(min = 2, max = 50, message = "Le nom court doit contenir entre 2 et 50 caractères")
    private String short_name;

    @NotBlank(message = "L'emplacement de l'agence est requis")
    @Size(max = 200, message = "L'emplacement ne peut pas dépasser 200 caractères")
    private String location;

    @NotNull(message = "L'ID de la gare routière est requis")
    private UUID gare_routiere_id;

    @Size(max = 100, message = "Le lien du réseau social ne peut pas dépasser 100 caractères")
    private String social_network;

    @Size(max = 500, message = "La description ne peut pas dépasser 500 caractères")
    private String description;

    @Size(max = 200, message = "Le message d'accueil ne peut pas dépasser 200 caractères")
    private String greeting_message;
}
