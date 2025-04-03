package kroryi.bus2.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
@RequiredArgsConstructor
@Log4j2
public class RedisLogService {
    private final RedisTemplate<String, Object> redisTemplate;
    private final CopyOnWriteArrayList<WebSocketSession> sessions = new CopyOnWriteArrayList<>();


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
            Properties info = connection.info();
            if (info == null) {
                log.error("❌ Redis 상태 정보가 NULL입니다.");
                return Map.of("error", "Redis 상태 정보 없음");
            }

            String usedMemory = info.getProperty("used_memory");
            String maxMemory = info.getProperty("maxmemory");
            String connectedClients = info.getProperty("connected_clients");

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

    public Map<String, String> fetchRedisStats() {
        try {
            Properties info = redisTemplate.getConnectionFactory().getConnection().info();

            log.info("🔍 Redis 상태 정보 조회 성공: {}", info);

            return Map.of(
                    "usedMemory", info.getProperty("used_memory"),
                    "maxMemory", info.getProperty("maxmemory"),
                    "connectedClients", info.getProperty("connected_clients")
            );
        } catch (Exception e) {
            log.error("❌ Redis 상태 조회 실패", e);
            return Map.of("error", "Failed to fetch Redis stats");
        }
    }

    public void broadcastRedisStats() {
        Map<String, String> redisStats = fetchRedisStats();
        String jsonResponse = String.format("{\"type\":\"redisStats\", \"data\":%s}", redisStats.toString());


        log.info("📡 Redis 상태 정보를 WebSocket으로 전송: {}", jsonResponse);

        sessions.forEach(session -> {
            try {
                session.sendMessage(new TextMessage(jsonResponse));
                log.info("✅ WebSocket 전송 성공: {}", session.getId());
            } catch (Exception e) {
                log.error("❌ WebSocket 전송 실패: {}", session.getId(), e);
            }
        });


    }
}