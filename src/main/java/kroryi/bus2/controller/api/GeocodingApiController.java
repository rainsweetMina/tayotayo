package kroryi.bus2.controller.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "지오코딩 API", description = "OpenStreetMap Nominatim API 프록시")
public class GeocodingApiController {

    private final WebClient.Builder webClientBuilder;
    
    @GetMapping("/reverse-geocode")
    @Operation(summary = "역지오코딩 API", description = "위도/경도 좌표를 주소로 변환합니다 (Nominatim API 프록시)")
    @Cacheable(value = "reverseGeocodeCache", key = "#lat + '-' + #lon", unless = "#result == null")
    public Mono<ResponseEntity<String>> reverseGeocode(
            @Parameter(description = "위도") @RequestParam double lat,
            @Parameter(description = "경도") @RequestParam double lon) {
        
        log.info("역지오코딩 요청: lat={}, lon={}", lat, lon);
        
        return webClientBuilder.build()
                .get()
                .uri("https://nominatim.openstreetmap.org/reverse?format=json&lat={lat}&lon={lon}&zoom=18&addressdetails=1",
                        lat, lon)
                .header("User-Agent", "TayoTayo/1.0 (contact@tayotayo.com)")
                .retrieve()
                .bodyToMono(String.class)
                .map(ResponseEntity::ok)
                .doOnError(e -> log.error("역지오코딩 API 호출 실패: {}", e.getMessage()))
                .onErrorResume(e -> Mono.just(ResponseEntity.status(500)
                        .body("{\"error\":\"Failed to fetch address\"}")));
    }
    
    @GetMapping("/geocode")
    @Operation(summary = "정방향 지오코딩 API", description = "주소를 위도/경도 좌표로 변환합니다 (Nominatim API 프록시)")
    @Cacheable(value = "geocodeCache", key = "#q", unless = "#result == null")
    public Mono<ResponseEntity<String>> geocode(
            @Parameter(description = "검색할 주소") @RequestParam String q,
            @Parameter(description = "결과 개수 제한") @RequestParam(required = false, defaultValue = "1") int limit) {
        
        log.info("정방향 지오코딩 요청: q={}, limit={}", q, limit);
        
        return webClientBuilder.build()
                .get()
                .uri("https://nominatim.openstreetmap.org/search?format=json&q={q}&limit={limit}",
                        q, limit)
                .header("User-Agent", "TayoTayo/1.0 (contact@tayotayo.com)")
                .retrieve()
                .bodyToMono(String.class)
                .map(ResponseEntity::ok)
                .doOnError(e -> log.error("정방향 지오코딩 API 호출 실패: {}", e.getMessage()))
                .onErrorResume(e -> Mono.just(ResponseEntity.status(500)
                        .body("{\"error\":\"Failed to fetch coordinates\"}")));
    }
} 