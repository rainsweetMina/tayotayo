package kroryi.bus2.controller.bus;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import kroryi.bus2.service.BusInfoInitService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Tag(name = "공공데이터 버스 기초 정보", description = "")
@RestController
@RequestMapping("/api/bus")
@RequiredArgsConstructor
@Log4j2
// 버스,노선 관련 데이터를 클라이언트에 제공하는 REST API 컨트롤러
// 이 컨트롤러는 JSON 형식으로 데이터를 반환하며, 클라이언트(웹/앱)에서 실시간 정보 조회 및 검색에 활용
public class BusDataController {
    private final BusInfoInitService busInfoInitService;
    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${api.service-key-decoding}")
    private String serviceKey;

    // 레디스 수동으로 지우는컨트롤러     조심히 다루세요
//    @Hidden
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Redis 전체 캐시 삭제", description = "Redis에 저장된 모든 캐시 데이터를 삭제합니다. 운영 환경에서는 주의해서 사용하세요.")
    @DeleteMapping("/evict/all")
    public ResponseEntity<String> evictAllCache() {
        Set<String> keys = redisTemplate.keys("*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
            log.info("[Redis] 전체 캐시 삭제됨. 삭제된 키 수: {}", keys.size());
            log.info("[Redis] 삭제된 키 목록:\n{}", String.join("\n", keys));
            return ResponseEntity.ok("모든 Redis 캐시가 삭제되었습니다. 삭제된 키 수: " + keys.size());
        } else {
            log.info("[Redis] 삭제할 캐시가 없습니다.");
            return ResponseEntity.ok("삭제할 캐시가 없습니다.");
        }
    }


    // 얘는 db에 기초종합정보 넣는거 이젠 쓰지마시길 렉 걸림 (나중에 하루에 한번 자동으로 실행되어 데이터 갱싱용으로 바꿀 예정)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "공공데이터 기초 종합 정보", description = "")
    @PostMapping("/fetch")
    public String fetchPostBusData() {
//        String routeId = "1000001000";
        String API_URL = "https://apis.data.go.kr/6270000/dbmsapi01/getBasic?";
        String apiUrl = API_URL + "serviceKey=" + URLEncoder.encode(serviceKey, StandardCharsets.UTF_8);

        busInfoInitService.fetchAndSaveBusData(apiUrl);
        return "Bus data fetched and saved successfully!";
    }

}
