package cm.yowyob.bus_station_backend.application.service;

import cm.yowyob.bus_station_backend.application.port.in.PolitiqueEtTaxesUseCase;
import cm.yowyob.bus_station_backend.application.port.out.PolitiqueEtTaxesPort;
import cm.yowyob.bus_station_backend.domain.model.PolitiqueEtTaxes;
import cm.yowyob.bus_station_backend.infrastructure.outbound.external.HttpMediaServiceAdapter;
import lombok.AllArgsConstructor;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
@AllArgsConstructor
public class PolitiqueEtTaxesService implements PolitiqueEtTaxesUseCase {

    private final PolitiqueEtTaxesPort politiqueEtTaxesPort;
    private final HttpMediaServiceAdapter httpMediaServiceAdapter;

    @Override
    public Mono<PolitiqueEtTaxes> createPolitique(PolitiqueEtTaxes politiqueEtTaxes, Mono<FilePart> filePartMono) {

        return Mono.just(politiqueEtTaxes)
            .flatMap(policy ->
                filePartMono
                    .flatMap(filePart -> httpMediaServiceAdapter.uploadFile(filePart, "politiques"))
                    .map(docUrl -> {
                        policy.setDocumentUrl(docUrl);
                        return policy;
                    })
                    .switchIfEmpty(Mono.just(policy))
            )
            .flatMap(politiqueEtTaxesPort::save);
    }

    @Override
    public Mono<PolitiqueEtTaxes> updatePolitique(UUID politiqueId, PolitiqueEtTaxes updatedPolitique, Mono<FilePart> filePartMono) {
        return politiqueEtTaxesPort.findById(politiqueId)
                .flatMap(existingPolitique -> {
                    Mono<PolitiqueEtTaxes> policyWithDoc = filePartMono
                        .flatMap(filePart -> httpMediaServiceAdapter.uploadFile(filePart, "politiques"))
                        .map(docUrl -> {
                            existingPolitique.setDocumentUrl(docUrl);
                            return existingPolitique;
                        });

                    return policyWithDoc.switchIfEmpty(Mono.just(existingPolitique));
                })
                .map(politique -> {
                    politique.setNomPolitique(updatedPolitique.getNomPolitique());
                    politique.setDescription(updatedPolitique.getDescription());
                    politique.setTauxTaxe(updatedPolitique.getTauxTaxe());
                    politique.setMontantFixe(updatedPolitique.getMontantFixe());
                    politique.setDateEffet(updatedPolitique.getDateEffet());
                    politique.setType(updatedPolitique.getType());
                    return politique;
                })
                .flatMap(politiqueEtTaxesPort::save);
    }


    @Override
    public Mono<Void> deletePolitique(UUID politiqueId) {
        return politiqueEtTaxesPort.deleteById(politiqueId);
    }

    @Override
    public Mono<PolitiqueEtTaxes> getById(UUID politiqueId) {
        return politiqueEtTaxesPort.findById(politiqueId);
    }

    @Override
    public Flux<PolitiqueEtTaxes> getAllByGareRoutiere(UUID gareRoutiereId) {
        return politiqueEtTaxesPort.findByGareRoutiereId(gareRoutiereId);
    }
}
