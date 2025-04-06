package kroryi.bus2.service;
import kroryi.bus2.repository.jpa.route.RouteRepository;
import kroryi.bus2.repository.redis.ApiLogRepository;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
@RequiredArgsConstructor
@Log4j2
public class RedisLogService {
    private final RedisTemplate<String, Object> redisTemplate;
    private final CopyOnWriteArrayList<WebSocketSession> sessions = new CopyOnWriteArrayList<>();
    private final ApiLogRepository apiLogRepository;
    private final RouteRepository routeRepository;


    // 메모리에 있는 Redis 읽기 (DB저장X)

    @Cacheable(value = "redisStats", key = "'info'")
    public Map<String, String> getRedisInfo() {
        log.info("캐시 미스 - Redis 정보를 직접 조회합니다.");
        return collectRedisStats();
    }

    @CachePut(value = "redisStats", key = "'info'")
    @Scheduled(fixedRate = 60000)  // 1분마다 캐시 갱신
    public Map<String, String> updateRedisInfo() {
        log.info("캐시 갱신 - Redis 정보를 다시 수집합니다.");
        return collectRedisStats();
    }

    // Redis 상태 정보 수집 메서드
    private Map<String, String> collectRedisStats() {
        try {
            // Redis 연결 팩토리 확인
            if (redisTemplate.getConnectionFactory() == null) {
                log.error("❌ Redis 연결 팩토리가 NULL입니다.");
                return Map.of("error", "Redis 연결 오류");
            }

            // Redis 연결 객체 확인
            var connection = redisTemplate.getConnectionFactory().getConnection();
            if (connection == null) {
                log.error("❌ Redis 연결이 NULL입니다.");
                return Map.of("error", "Redis 연결 오류");
            }

            // Redis 상태 정보 수집
            Properties info = redisTemplate.getConnectionFactory().getConnection().info();

            String usedMemory = info.getProperty("used_memory","0");
            String maxMemory = info.getProperty("maxmemory","0");
            String connectedClients = info.getProperty("connected_clients","0");

            // Null 체크 후 기본 값으로 대체
            usedMemory = (usedMemory != null) ? usedMemory : "0";
            maxMemory = (maxMemory != null) ? maxMemory : "0";
            connectedClients = (connectedClients != null) ? connectedClients : "0";

            // Byte를 KB로 변환
            long usedMemoryKb = Long.parseLong(usedMemory) / 1024;
            long maxMemoryKb = Long.parseLong(maxMemory) / 1024;

            Map<String, String> stats = Map.of(
                    "usedMemory", String.valueOf(usedMemoryKb) + " KB",
                    "maxMemory", String.valueOf(maxMemoryKb) + " KB",
                    "connectedClients", connectedClients
            );

            log.info("🔍 Redis 메모리 사용량: {}/{}", usedMemoryKb + " KB", maxMemoryKb + " KB");
            log.info("🔗 Redis 클라이언트 연결 수: {}", connectedClients);

            return stats;
        } catch (Exception e) {
            log.error("❌ Redis 상태 수집 중 오류 발생", e);
            return Map.of("error", "정보 수집 실패");
        }

    }

    // 설정 끝


    // RedisLogService에서 주기적으로 데이터 수집 후 WebSocket으로 전송

    public Map<String, Object> fetchRedisStats() {
        Map<String, Object> stats = new HashMap<>();
        try {
            Properties info = redisTemplate.getConnectionFactory().getConnection().info();

            log.info("🔍 Redis 상태 정보 조회 성공: {}", info);


            stats.put("usedMemory", Integer.parseInt(info.getProperty("used_memory", "0")));
            stats.put("connectedClients", Integer.parseInt(info.getProperty("connected_clients", "0")));
//            stats.put("maxMemory", info.getProperty("maxmemory", "0")); // 아직 안넣었음.


//            // Routes Count, Requests Today 받아오는 쿼리인데, Redis 메모리값 읽어오는거라 복잡한 쿼리가 실행 안됨.
//            long routeCount = routeRepository.count();  // Route 개수
//            long requestCountToday = apiLogRepository.countByTimestampBetween(
//                    LocalDate.now().atStartOfDay(),
//                    LocalDate.now().plusDays(1).atStartOfDay()
//            );
//            stats.put("routesCount", String.valueOf(routeCount));
//            stats.put("requestToday", String.valueOf(requestCountToday));



        } catch (Exception e) {
            log.error("❌ Redis 상태 조회 실패", e);
            return Map.of("error", "Failed to fetch Redis stats");
        }
        return stats;

    }

    private String formatMemory(String memoryInBytes) {
        try {
            long bytes = Long.parseLong(memoryInBytes);
            if (bytes >= 1024 * 1024) {
                return String.format("%.2f MB", bytes / (1024.0 * 1024.0));
            } else if (bytes >= 1024) {
                return String.format("%.2f KB", bytes / 1024.0);
            } else {
                return bytes + " B";
            }
        } catch (NumberFormatException e) {
            return "0 B";
        }


    }

    // WebSocket 세션 관리
    public void broadcastRedisStats() {
        Map<String, Object> redisStats = fetchRedisStats();
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            // Map을 JSON 문자열로 변환
            String jsonResponse = objectMapper.writeValueAsString(Map.of("type", "redisStats", "data", redisStats));

            log.info("📡 Redis 상태 정보를 WebSocket으로 전송: {}", jsonResponse);

            sessions.forEach(session -> {
                try {
                    session.sendMessage(new TextMessage(jsonResponse));
                    log.info("✅ WebSocket 전송 성공: {}", session.getId());
                } catch (Exception e) {
                    log.error("❌ WebSocket 전송 실패: {}", session.getId(), e);
                }
            });
        } catch (JsonProcessingException e) {
            log.error("❌ JSON 변환 실패", e);
        }
    }



}
