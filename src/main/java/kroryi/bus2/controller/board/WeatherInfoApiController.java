package kroryi.bus2.controller.board;

import io.swagger.v3.oas.annotations.Hidden;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Hidden
@RestController
@RequestMapping("/api/public")
@Log4j2
public class WeatherInfoApiController {
    @Value("${public.api-key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    @GetMapping("/api-key")
    public Map<String, String> getApiKey() {
        // JWT 토큰 확인 (있으면 로그)
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal().toString())) {
            log.info("인증된 사용자가 API 키를 요청했습니다: {}", auth.getName());
        } else {
            log.info("인증되지 않은 사용자가 API 키를 요청했습니다");
        }
        
        log.trace("getApiKey-------------> {}", apiKey);
        return Map.of("apiKey", apiKey);
    }

    /**
     * 미세먼지 정보를 가져오는 API
     * @param stationName 측정소 이름 (예: 중구)
     * @param dataTerm 데이터 기간 (예: DAILY)
     * @return 미세먼지 정보
     */
    @GetMapping("/air-quality")
    public ResponseEntity<Object> getAirQuality(
            @RequestParam(defaultValue = "중구") String stationName,
            @RequestParam(defaultValue = "DAILY") String dataTerm) {
        try {
            // JWT 토큰 확인 (있으면 로그)
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal().toString())) {
                log.info("인증된 사용자가 미세먼지 정보를 요청했습니다: {}", auth.getName());
            } else {
                log.info("인증되지 않은 사용자가 미세먼지 정보를 요청했습니다");
            }
            
            log.info("미세먼지 정보 요청: stationName={}, dataTerm={}", stationName, dataTerm);
            
            // URL 인코딩
            String encodedStationName = URLEncoder.encode(stationName, StandardCharsets.UTF_8);
            String encodedApiKey = URLEncoder.encode(apiKey, StandardCharsets.UTF_8);
            
            // API URL 구성
            String url = "https://apis.data.go.kr/B552584/ArpltnInforInqireSvc/getMsrstnAcctoRltmMesureDnsty" +
                    "?serviceKey=" + encodedApiKey +
                    "&returnType=json" +
                    "&numOfRows=1" +
                    "&pageNo=1" +
                    "&stationName=" + encodedStationName +
                    "&dataTerm=" + dataTerm +
                    "&ver=1.3";
            
            // API 호출
            ResponseEntity<Object> response = restTemplate.getForEntity(new URI(url), Object.class);
            log.info("미세먼지 API 응답: {}", response.getStatusCode());
            
            return response;
        } catch (Exception e) {
            log.error("미세먼지 정보 조회 중 오류 발생", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", "미세먼지 정보를 가져오는 중 오류가 발생했습니다.",
                    "message", e.getMessage()
            ));
        }
    }
    
    /**
     * 날씨 정보를 가져오는 API
     * @param baseDate 기준 날짜 (예: 20250624)
     * @param baseTime 기준 시간 (예: 1100)
     * @param nx X 좌표
     * @param ny Y 좌표
     * @return 날씨 정보
     */
    @GetMapping("/weather")
    public ResponseEntity<Object> getWeather(
            @RequestParam String baseDate,
            @RequestParam String baseTime,
            @RequestParam int nx,
            @RequestParam int ny) {
        try {
            // JWT 토큰 확인 (있으면 로그)
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal().toString())) {
                log.info("인증된 사용자가 날씨 정보를 요청했습니다: {}", auth.getName());
            } else {
                log.info("인증되지 않은 사용자가 날씨 정보를 요청했습니다");
            }
            
            log.info("날씨 정보 요청: baseDate={}, baseTime={}, nx={}, ny={}", baseDate, baseTime, nx, ny);
            
            // URL 인코딩
            String encodedApiKey = URLEncoder.encode(apiKey, StandardCharsets.UTF_8);
            
            // API URL 구성
            String url = "https://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getUltraSrtFcst" +
                    "?serviceKey=" + encodedApiKey +
                    "&pageNo=1" +
                    "&numOfRows=100" +
                    "&dataType=JSON" +
                    "&base_date=" + baseDate +
                    "&base_time=" + baseTime +
                    "&nx=" + nx +
                    "&ny=" + ny;
            
            // API 호출
            ResponseEntity<Object> response = restTemplate.getForEntity(new URI(url), Object.class);
            log.info("날씨 API 응답: {}", response.getStatusCode());
            
            return response;
        } catch (Exception e) {
            log.error("날씨 정보 조회 중 오류 발생", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", "날씨 정보를 가져오는 중 오류가 발생했습니다.",
                    "message", e.getMessage()
            ));
        }
    }
}
