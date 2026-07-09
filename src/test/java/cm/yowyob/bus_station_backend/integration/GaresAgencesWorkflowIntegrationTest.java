package cm.yowyob.bus_station_backend.integration;

import cm.yowyob.bus_station_backend.BaseIntegrationTest;
import cm.yowyob.bus_station_backend.application.dto.affiliation.AffiliationCreateDTO;
import cm.yowyob.bus_station_backend.application.dto.agence.AgenceVoyageDTO;
import cm.yowyob.bus_station_backend.application.dto.gareRoutiere.GareRoutiereRequestDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Tests d'intégration - Workflow Gares et Agences")
class GaresAgencesWorkflowIntegrationTest extends BaseIntegrationTest {

    @Test
    @DisplayName("Workflow complet : Création et update Gare + Agence")
    void completeGaresAgencesWorkflow() {
        // 1. Création Gare
        GareRoutiereRequestDTO gareDTO = new GareRoutiereRequestDTO();
        gareDTO.setNomGareRoutiere("Gare de Yaoundé");
        gareDTO.setVille("Yaoundé");
        gareDTO.setQuartier("Mvan");
        gareDTO.setNomPresident("Président Test");
        gareDTO.setManagerId(testAdminId); 

        var createdGare = webTestClient.post()
                .uri("/gare")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(gareDTO)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(cm.yowyob.bus_station_backend.domain.model.GareRoutiere.class)
                .returnResult()
                .getResponseBody();

        assertThat(createdGare).isNotNull();

        // 2. Création Organisation
        UUID orgId = UUID.randomUUID();
        databaseClient.sql("INSERT INTO organization (id, organization_id, status) VALUES (:id, :orgId, 'ACTIVE')")
                .bind("id", orgId)
                .bind("orgId", orgId)
                .then().block();

        // 2. Création Agence
        AgenceVoyageDTO agenceDTO = new AgenceVoyageDTO();
        agenceDTO.setLong_name("Agence Test Transport");
        agenceDTO.setShort_name("ATT");
        agenceDTO.setLocation("Yaoundé");
        agenceDTO.setUser_id(testAdminId);
        agenceDTO.setOrganisation_id(orgId);
        agenceDTO.setGare_routiere_id(createdGare.getIdGareRoutiere()); // Set here

        var createdAgence = webTestClient.post()
                .uri("/agence")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(agenceDTO)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(cm.yowyob.bus_station_backend.application.dto.agence.AgenceVoyageResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertThat(createdAgence).isNotNull();

        // 3. GET Agence
        webTestClient.get()
                .uri("/agence/{id}", createdAgence.getId())
                .header("Authorization", "Bearer " + adminToken)
                .exchange()
                .expectStatus().isOk()
                .expectBody(cm.yowyob.bus_station_backend.application.dto.agence.AgenceVoyageResponseDTO.class)
                .value(agence -> {
                    assertThat(agence.getId()).isEqualTo(createdAgence.getId());
                    assertThat(agence.getLongName()).isEqualTo("Agence Test Transport");
                });

        // 4. Update Agence
        agenceDTO.setLong_name("Agence Test Transport Modifiée");
        webTestClient.patch()
                .uri("/agence/{id}", createdAgence.getId())
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(agenceDTO)
                .exchange()
                .expectStatus().isOk()
                .expectBody(cm.yowyob.bus_station_backend.application.dto.agence.AgenceVoyageDTO.class)
                .value(agence -> {
                    assertThat(agence.getLong_name()).isEqualTo("Agence Test Transport Modifiée");
                });

        agenceDTO.setGare_routiere_id(createdGare.getIdGareRoutiere()); 
        
        cm.yowyob.bus_station_backend.application.dto.affiliation.AffiliationCreateDTO affiliationDTO = new cm.yowyob.bus_station_backend.application.dto.affiliation.AffiliationCreateDTO();
        affiliationDTO.setGareRoutiereId(createdGare.getIdGareRoutiere());
        affiliationDTO.setAgencyId(createdAgence.getId());
        affiliationDTO.setEcheance(java.time.LocalDate.now().plusMonths(1));
        affiliationDTO.setMontantAffiliation(10000.0);

        webTestClient.post()
                .uri("/affiliation")
                .header("Authorization", "Bearer " + bsmToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(affiliationDTO)
                .exchange()
                .expectStatus().isCreated();
        }
        }
