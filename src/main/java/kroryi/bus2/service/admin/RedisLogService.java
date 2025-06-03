package kroryi.bus2.service.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import kroryi.bus2.dto.RedisMemoryInfo;
import kroryi.bus2.dto.RedisStats;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisLogService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    private static final String REDIS_STATS_CACHE = "redisStats";
    private static final String REDIS_INFO_KEY = "info";
    private static final String REDIS_MEMORY_TOPIC = "/topic/redis-memory";
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    /**
     * Redis 정보를 캐시에서 조회
     */
    @Cacheable(value = REDIS_STATS_CACHE, key = "'" + REDIS_INFO_KEY + "'")
    public RedisStats getRedisInfo() {
        log.info("캐시 미스 - Redis 정보를 직접 조회합니다.");
        return collectRedisStats();
    }

    /**
     * Redis 정보를 주기적으로 갱신 (10분마다)
     */
    @CachePut(value = REDIS_STATS_CACHE, key = "'" + REDIS_INFO_KEY + "'")
    @Scheduled(fixedRate = 60000) // 1분마다 갱신
    public RedisStats updateRedisInfo() {
        log.info("캐시 갱신 - Redis 정보를 다시 수집합니다.");
        return collectRedisStats();
    }

    /**
     * Redis 메모리 정보를 실시간으로 WebSocket을 통해 전송 (1분마다)
     */
    @Scheduled(fixedRate = 60000) // 1분으로 변경
    public void sendRedisMemoryInfo() {
        try {
            RedisMemoryInfo memoryInfo = collectRedisMemoryInfo();
            messagingTemplate.convertAndSend(REDIS_MEMORY_TOPIC, memoryInfo);
            log.debug("Redis 메모리 정보 전송 완료: {}", memoryInfo);
        } catch (Exception e) {
            log.error("Redis 메모리 정보 전송 실패", e);
        }
    }

    /**
     * Redis 상태 정보 수집
     */
    private RedisStats collectRedisStats() {
        try {
            if (redisTemplate.getConnectionFactory() == null) {
                log.error("Redis 연결 팩토리가 NULL입니다.");
                return RedisStats.createError("Redis 연결 오류");
            }

            var connection = redisTemplate.getConnectionFactory().getConnection();
            if (connection == null) {
                log.error("Redis 연결이 NULL입니다.");
                return RedisStats.createError("Redis 연결 오류");
            }

            Properties info = connection.info();
            if (info == null) {
                log.error("Redis 상태 정보가 NULL입니다.");
                return RedisStats.createError("Redis 상태 정보 없음");
            }

            return RedisStats.builder()
                    .usedMemory(parseMemoryValue(info.getProperty("used_memory", "0")))
                    .maxMemory(parseMemoryValue(info.getProperty("maxmemory", "0")))
                    .connectedClients(Integer.parseInt(info.getProperty("connected_clients", "0")))
                    .uptime(info.getProperty("uptime_in_seconds", "0"))
                    .version(info.getProperty("redis_version", "unknown"))
                    .build();
        } catch (Exception e) {
            log.error("Redis 상태 수집 중 오류 발생", e);
            return RedisStats.createError("정보 수집 실패");
        }
    }

    /**
     * Redis 메모리 정보만 수집
     */
    private RedisMemoryInfo collectRedisMemoryInfo() {
        try {
            if (redisTemplate.getConnectionFactory() == null) {
                log.error("Redis 연결 팩토리가 NULL입니다.");
                return createEmptyMemoryInfo();
            }

            var connection = redisTemplate.getConnectionFactory().getConnection();
            if (connection == null) {
                log.error("Redis 연결이 NULL입니다.");
                return createEmptyMemoryInfo();
            }

            Properties info = connection.info();
            if (info == null) {
                log.error("Redis 정보가 NULL입니다.");
                return createEmptyMemoryInfo();
            }

            String usedMemoryStr = info.getProperty("used_memory", "0");
            String maxMemoryStr = info.getProperty("maxmemory", "0");
            String connectedClientsStr = info.getProperty("connected_clients", "0");

            log.info("Redis 원본 정보 - used_memory: {}, maxmemory: {}, connected_clients: {}", 
                    usedMemoryStr, maxMemoryStr, connectedClientsStr);

            double usedMemoryMB = convertToMegabytes(usedMemoryStr);
            double maxMemoryMB = convertToMegabytes(maxMemoryStr);
            int connectedClients = Integer.parseInt(connectedClientsStr);

            Set<String> routeKeys = redisTemplate.keys("routeStats::*");
            Set<String> statsKeys = redisTemplate.keys("routeStats::*");
            
            long routeCount = (routeKeys != null) ? routeKeys.size() : 0;
            long requestCountToday = (statsKeys != null) ? statsKeys.size() : 0;

            log.debug("Redis 키 조회 결과 - routeKeys: {}, statsKeys: {}", 
                    routeCount, requestCountToday);

            RedisMemoryInfo memoryInfo = RedisMemoryInfo.builder()
                    .time(LocalDateTime.now().format(TIME_FORMATTER))
                    .usedMemory(usedMemoryMB)
                    .maxMemory(maxMemoryMB)
                    .connectedClients(connectedClients)
                    .routesCount(routeCount)
                    .requestToday(requestCountToday)
                    .build();

            log.info("Redis 메모리 정보 - 사용 메모리: {}MB, 최대 메모리: {}MB, 연결된 클라이언트: {}", 
                    String.format("%.2f", usedMemoryMB), 
                    String.format("%.2f", maxMemoryMB), 
                    connectedClients);
            return memoryInfo;

        } catch (Exception e) {
            log.error("Redis 메모리 정보 수집 실패", e);
            return createEmptyMemoryInfo();
        }
    }

    private double convertToMegabytes(String bytes) {
        try {
            double value = Double.parseDouble(bytes);
            return value / (1024.0 * 1024.0);
        } catch (NumberFormatException e) {
            log.error("메모리 값 변환 실패. 입력값: {}", bytes, e);
            return 0.0;
        }
    }

    private RedisMemoryInfo createEmptyMemoryInfo() {
        return RedisMemoryInfo.builder()
                .time(LocalDateTime.now().format(TIME_FORMATTER))
                .usedMemory(0.0)
                .maxMemory(0.0)
                .connectedClients(0)
                .routesCount(0)
                .requestToday(0)
                .build();
    }

    /**
     * 메모리 값을 바이트에서 MB로 변환
     */
    private long parseMemoryValue(String memoryInBytes) {
        try {
            double bytes = Double.parseDouble(memoryInBytes);
            double megabytes = bytes / (1024.0 * 1024.0);
            log.debug("메모리 변환 - 원본: {} bytes, 변환: {} MB", 
                    bytes, String.format("%.2f", megabytes));
            return Math.round(megabytes); // 반올림하여 가장 가까운 정수값 반환
        } catch (NumberFormatException e) {
            log.error("메모리 값 변환 실패. 입력값: {}", memoryInBytes, e);
            return 0;
        }
    }
}

