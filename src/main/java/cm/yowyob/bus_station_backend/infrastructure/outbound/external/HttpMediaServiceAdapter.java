package cm.yowyob.bus_station_backend.infrastructure.outbound.external;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class HttpMediaServiceAdapter {

  private final WebClient webClient;
  private final String serviceName;
  private final String mediaServiceBaseUrl;

  public HttpMediaServiceAdapter(WebClient.Builder builder,
      @Value("${application.external.media-service-url}") String mediaUrl,
      @Value("${application.external.media-service-name}") String serviceName) {
    this.webClient = builder.baseUrl(mediaUrl).build();
    this.mediaServiceBaseUrl = mediaUrl;
    this.serviceName = serviceName;
  }

  public Mono<String> uploadFile(FilePart file, String location) {
    MultipartBodyBuilder builder = new MultipartBodyBuilder();

    MediaType contentType = file.headers().getContentType();
    if (contentType == null)
      contentType = MediaType.APPLICATION_OCTET_STREAM;

    builder.asyncPart("file", file.content(), DataBuffer.class)
        .filename(file.filename())
        .contentType(contentType);

    builder.part("service", serviceName != null ? serviceName : "UGATE_DEFAULT");
    builder.part("location", location);

    return webClient.post()
        .uri("/media/upload")
        .body(BodyInserters.fromMultipartData(builder.build()))
        .retrieve()
        .onStatus(status -> status.isError(), response -> {
          return response.bodyToMono(String.class)
              .flatMap(errorBody -> Mono.error(new RuntimeException(
                  "Erreur Media Service (" + response.statusCode() + ") : " + errorBody)));
        })
        .bodyToMono(MediaResponseDTO.class)
        .map(this::constructPublicUrl)
        .doOnError(e -> log.error("Échec upload vers {} : {}", location, e.getMessage()));
  }

  /**
   * Construit l'URL publique de téléchargement.
   * Utilise l'endpoint /media/proxy/{id} défini dans le Swagger du Media Service.
   */
  private String constructPublicUrl(MediaResponseDTO dto) {
    String baseUrl = this.mediaServiceBaseUrl.endsWith("/")
        ? this.mediaServiceBaseUrl.substring(0, this.mediaServiceBaseUrl.length() - 1)
        : this.mediaServiceBaseUrl;

    return baseUrl + "/media/proxy/" + dto.id();
  }

  record MediaResponseDTO(String id, String uri, String path, String name, String mime) {
  }
}