package cm.yowyob.bus_station_backend.integration;

import cm.yowyob.bus_station_backend.BaseIntegrationTest;
import cm.yowyob.bus_station_backend.application.dto.affiliation.AffiliationResponseDTO;
import cm.yowyob.bus_station_backend.domain.enums.StatutTaxe;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class AffiliationAgenceVoyageControllerIntegrationTest extends BaseIntegrationTest {

    @Test
    void getAffiliationsByGareRoutiereId_shouldReturnAffiliations() {
        UUID gareRoutiereId = UUID.randomUUID();
        UUID agencyId1 = UUID.randomUUID();
        UUID agencyId2 = UUID.randomUUID();

        insertAffiliationInDb(UUID.randomUUID(), gareRoutiereId, agencyId1, "Agency 1", 50000.0).block();
        insertAffiliationInDb(UUID.randomUUID(), gareRoutiereId, agencyId2, "Agency 2", 75000.0).block();

        authenticatedClient(bsmToken).get()
                .uri("/affiliation/gare/{gareId}", gareRoutiereId)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(AffiliationResponseDTO.class)
                .hasSize(2)
                .consumeWith(response -> {
                    assertThat(response.getResponseBody()).isNotNull();
                    assertThat(response.getResponseBody()).extracting(AffiliationResponseDTO::getGareRoutiereId)
                            .containsOnly(gareRoutiereId);
                    assertThat(response.getResponseBody()).extracting(AffiliationResponseDTO::getAgencyName)
                            .containsExactlyInAnyOrder("Agency 1", "Agency 2");
                });
    }

    @Test
    void getAffiliationsByGareRoutiereId_shouldReturnEmptyList_whenNoAffiliationsExist() {
        UUID nonExistentGareRoutiereId = UUID.randomUUID();

        authenticatedClient(bsmToken).get()
                .uri("/affiliation/gare/{gareId}", nonExistentGareRoutiereId)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(AffiliationResponseDTO.class)
                .hasSize(0);
    }

    private Mono<Void> insertAffiliationInDb(UUID id, UUID gareRoutiereId, UUID agencyId, String agencyName,
                                           Double montantAffiliation) {
        return databaseClient.sql("""
                INSERT INTO affiliation_agence_voyage (id, gare_routiere_id, agency_id, agency_name, statut, echeance, montant_affiliation, created_at, updated_at)
                VALUES (:id, :gareId, :agencyId, :agencyName, :statut, :echeance, :montantAffiliation, :createdAt, :updatedAt)
                """)
                .bind("id", id)
                .bind("gareId", gareRoutiereId)
                .bind("agencyId", agencyId)
                .bind("agencyName", agencyName)
                .bind("statut", StatutTaxe.PAYE.name())
                .bind("echeance", LocalDate.now().plusMonths(1))
                .bind("montantAffiliation", montantAffiliation)
                .bind("createdAt", LocalDateTime.now())
                .bind("updatedAt", LocalDateTime.now())
                .fetch()
                .rowsUpdated()
                .then();
    }
}
