package kroryi.bus2.controller.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "지오코딩 API", description = "OpenStreetMap Nominatim API 프록시")
public class GeocodingApiController {

    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper;
    
    @GetMapping("/reverse-geocode")
    @Operation(summary = "역지오코딩 API", description = "위도/경도 좌표를 주소로 변환합니다 (Nominatim API 프록시)")
    @Cacheable(value = "reverseGeocodeCache", key = "#lat + '-' + #lon", unless = "#result == null")
    public ResponseEntity<Map<String, Object>> reverseGeocode(
            @Parameter(description = "위도") @RequestParam double lat,
            @Parameter(description = "경도") @RequestParam double lon) {
        
        log.info("역지오코딩 요청: lat={}, lon={}", lat, lon);
        
        // 기본 응답 생성 (API 호출 실패 시 사용)
        Map<String, Object> fallbackResponse = new HashMap<>();
        fallbackResponse.put("address", "주소 정보 없음");
        fallbackResponse.put("roadAddress", "주소 정보 없음");
        fallbackResponse.put("region1", "대구광역시");
        fallbackResponse.put("region2", "중구");
        fallbackResponse.put("region3", "");
        fallbackResponse.put("lat", lat);
        fallbackResponse.put("lon", lon);
        fallbackResponse.put("error", true);
        
        try {
            String response = webClientBuilder.build()
                    .get()
                    .uri("https://nominatim.openstreetmap.org/reverse?format=json&lat={lat}&lon={lon}&zoom=18&addressdetails=1",
                            lat, lon)
                    .header("User-Agent", "TayoTayo/1.0 (contact@tayotayo.com)")
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(3))
                    .retryWhen(Retry.backoff(1, Duration.ofSeconds(1))
                            .filter(throwable -> !(throwable instanceof ResponseStatusException)))
                    .doOnError(e -> log.error("역지오코딩 API 호출 실패: {}", e.getMessage()))
                    .onErrorReturn("{}")
                    .block(); // Mono를 동기적으로 처리
            
            log.info("Nominatim API 응답: {}", response);
            
            if (response != null && !response.trim().isEmpty() && !response.equals("{}")) {
                JsonNode jsonNode = objectMapper.readTree(response);
                
                Map<String, Object> result = new HashMap<>();
                
                // display_name 추출
                if (jsonNode.has("display_name")) {
                    String displayName = jsonNode.get("display_name").asText();
                    result.put("address", displayName);
                    result.put("roadAddress", displayName);
                }
                
                // address 객체에서 상세 정보 추출
                if (jsonNode.has("address")) {
                    JsonNode addressNode = jsonNode.get("address");
                    
                    if (addressNode.has("city")) {
                        result.put("region1", addressNode.get("city").asText());
                    } else if (addressNode.has("province")) {
                        result.put("region1", addressNode.get("province").asText());
                    }
                    
                    if (addressNode.has("county")) {
                        result.put("region2", addressNode.get("county").asText());
                    } else if (addressNode.has("district")) {
                        result.put("region2", addressNode.get("district").asText());
                    }
                    
                    if (addressNode.has("suburb")) {
                        result.put("region3", addressNode.get("suburb").asText());
                    } else if (addressNode.has("neighbourhood")) {
                        result.put("region3", addressNode.get("neighbourhood").asText());
                    }
                }
                
                // 좌표 정보 추가
                result.put("lat", lat);
                result.put("lon", lon);
                result.put("error", false);
                
                log.info("구조화된 주소 정보: {}", result);
                return ResponseEntity.ok(result);
            } else {
                log.warn("Nominatim API에서 빈 응답 반환, 기본값 사용");
                return ResponseEntity.ok(fallbackResponse);
            }
            
        } catch (Exception e) {
            log.error("역지오코딩 처리 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.ok(fallbackResponse);
        }
    }
    
    @GetMapping("/geocode")
    @Operation(summary = "정방향 지오코딩 API", description = "주소를 위도/경도 좌표로 변환합니다 (Nominatim API 프록시)")
    @Cacheable(value = "geocodeCache", key = "#q != null ? #q : #address", unless = "#result == null")
    public Mono<String> geocode(
            @Parameter(description = "검색할 주소") @RequestParam(required = false) String q,
            @Parameter(description = "검색할 주소 (q와 동일, 프론트엔드 호환용)") @RequestParam(required = false) String address,
            @Parameter(description = "결과 개수 제한") @RequestParam(required = false, defaultValue = "1") int limit) {
        
        // address 파라미터를 q로 대체 (둘 다 제공되면 q 우선)
        String query = q != null ? q : address;
        if (query == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "주소 파라미터(q 또는 address)가 필요합니다");
        }
        
        log.info("정방향 지오코딩 요청: query={}, limit={}", query, limit);
        
        return webClientBuilder.build()
                .get()
                .uri("https://nominatim.openstreetmap.org/search?format=json&q={query}&limit={limit}",
                        query, limit)
                .header("User-Agent", "TayoTayo/1.0 (contact@tayotayo.com)")
                .retrieve()
                .bodyToMono(String.class)
                // 타임아웃 설정 (3초)
                .timeout(Duration.ofSeconds(3))
                // 최대 1회 재시도
                .retryWhen(Retry.backoff(1, Duration.ofSeconds(1))
                        .filter(throwable -> !(throwable instanceof ResponseStatusException)))
                .doOnError(e -> log.error("정방향 지오코딩 API 호출 실패: {}", e.getMessage()))
                .onErrorResume(e -> Mono.error(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                        "Failed to fetch coordinates", e)));
    }
} 