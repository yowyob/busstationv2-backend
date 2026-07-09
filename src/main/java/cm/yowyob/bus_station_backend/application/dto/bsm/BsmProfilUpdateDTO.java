// src/main/java/cm/yowyob/bus_station_backend/application/dto/bsm/BsmProfilUpdateDTO.java

package cm.yowyob.bus_station_backend.application.dto.bsm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Payload PATCH-like pour PUT /bsm/profil. Champs null = ignorés.
 * NB : `address` non supporté pour l'instant — UserDTO/UserMapper ne le gèrent pas.
 *      À ajouter en LOT 11 (extension UserDTO + updateUserFromDTO).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BsmProfilUpdateDTO {
    private String nom;
    private String prenom;
    private String email;
    private String telNumber;
}
