package cm.yowyob.bus_station_backend.infrastructure.inbound.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class PingController {

    @GetMapping("/ping")
    public Mono<ResponseEntity<String>> ping() {
        return Mono.just(ResponseEntity.ok("OK"));
    }
}
