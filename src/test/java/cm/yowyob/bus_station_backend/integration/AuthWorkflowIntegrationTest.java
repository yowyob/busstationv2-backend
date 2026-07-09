package cm.yowyob.bus_station_backend.integration;

import cm.yowyob.bus_station_backend.BaseIntegrationTest;
import cm.yowyob.bus_station_backend.application.dto.auth.AuthTokensDTO;
import cm.yowyob.bus_station_backend.application.dto.auth.ForgotPasswordRequestDTO;
import cm.yowyob.bus_station_backend.application.dto.auth.ResetPasswordRequestDTO;
import cm.yowyob.bus_station_backend.application.dto.user.AuthentificationDTO;
import cm.yowyob.bus_station_backend.application.dto.user.UserDTO;
import cm.yowyob.bus_station_backend.domain.enums.Gender;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Tests d'intégration - Workflow Authentification")
class AuthWorkflowIntegrationTest extends BaseIntegrationTest {

    @Test
    @DisplayName("Workflow complet : Inscription, Connexion, Profil, Mot de passe")
    void completeAuthWorkflow() {
        // 1. Inscription
        UserDTO userDTO = new UserDTO();
        userDTO.setEmail("test@yowyob.cm");
        userDTO.setUsername("testuser");
        userDTO.setPassword("Password123!");
        userDTO.setLast_name("Test");
        userDTO.setFirst_name("User");
        userDTO.setPhone_number("6" + System.currentTimeMillis());
        userDTO.setGender(Gender.MALE);
        userDTO.setRole(java.util.List.of(cm.yowyob.bus_station_backend.domain.enums.RoleType.USAGER));

        webTestClient.post()
                .uri("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(userDTO)
                .exchange()
                .expectStatus().isCreated();

        // 2. Connexion
        AuthentificationDTO loginDTO = new AuthentificationDTO("testuser", "Password123!");
        AuthTokensDTO tokens = webTestClient.post()
                .uri("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(loginDTO)
                .exchange()
                .expectStatus().isOk()
                .expectBody(AuthTokensDTO.class)
                .returnResult()
                .getResponseBody();

        assertThat(tokens).isNotNull();
        assertThat(tokens.getAccessToken()).isNotNull();

        // 3. Récupération profil
        webTestClient.get()
                .uri("/auth/me")
                .header("Authorization", "Bearer " + tokens.getAccessToken())
                .exchange()
                .expectStatus().isOk();

        // 4. Forgot Password
        ForgotPasswordRequestDTO forgotDTO = new ForgotPasswordRequestDTO("test@yowyob.cm");
        Map<String, String> forgotResponse = webTestClient.post()
                .uri("/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(forgotDTO)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Map.class)
                .returnResult()
                .getResponseBody();

        String resetToken = forgotResponse.get("debugToken");
        assertThat(resetToken).isNotNull();

        // 5. Reset Password
        ResetPasswordRequestDTO resetDTO = new ResetPasswordRequestDTO(resetToken, "NewPassword123!");
        webTestClient.post()
                .uri("/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(resetDTO)
                .exchange()
                .expectStatus().isNoContent();
    }
}
