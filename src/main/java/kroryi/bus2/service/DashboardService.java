package kroryi.bus2.service;


import kroryi.bus2.dto.lost.LostStatResponseDTO;
import kroryi.bus2.repository.jpa.FoundItemRepository;
import kroryi.bus2.repository.jpa.LostFoundMatchRepository;
import kroryi.bus2.repository.jpa.LostItemRepository;
import kroryi.bus2.repository.redis.ApiLogRepository;
import kroryi.bus2.repository.jpa.RouteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;


import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Log4j2
public class DashboardService {


    //분실물 통계관련 시작
    private final LostItemRepository lostItemRepository;
    private final FoundItemRepository foundItemRepository;
    private final LostFoundMatchRepository lostFoundMatchRepository;
    //분실물 통계관련 종료


    private final RouteRepository routeRepository;
    private final ApiLogRepository apiLogRepository;
    private final RedisLogService redisLogService;


    // 🔵 분실물 통계 메서드 시작
    public LostStatResponseDTO getLostStats() {
        long reported = lostItemRepository.count();
        long found = foundItemRepository.count();
        long matched = lostFoundMatchRepository.count();

        return new LostStatResponseDTO(reported, found, matched);
    }
    // 🔵 분실물 통계 메서드 종료



    // 검색량, 오늘 요구량, 레디스 사용량 받기

    // 대시보드 통계 데이터 수집
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> result = new HashMap<>();

        long routeCount = routeRepository.count();
        long requestCountToday = apiLogRepository.count();

        // Redis 상태 정보 가져오기
        Map<String, String> redisStats = redisLogService.getRedisInfo();

        String usedMemory = redisStats.getOrDefault("usedMemory", "0");
        String maxMemory = redisStats.getOrDefault("maxMemory", "0");
        String connectedClients = redisStats.getOrDefault("connectedClients", "0");

        result.put("routesCount", routeCount);
        result.put("requestToday", requestCountToday);
        result.put("redisUsedMemory", usedMemory);
        result.put("redisMaxMemory", maxMemory);
        result.put("redisConnectedClients", connectedClients);

        log.info("📊 대시보드 통계 데이터 수집 완료: {}", result);
        return result;
    }


    // 검색량, 오늘 요구량, 레디스 사용량 받기
//    public Map<String, Object> getDashboardStats() {
//        Map<String, Object> result = new HashMap<>();
//
//        long routeCount = routeRepository.count();
//        long requestCountToday = apiLogRepository.countByTimestampBetween(
//                LocalDate.now().atStartOfDay(),
//                LocalDate.now().plusDays(1).atStartOfDay()
//        );
//
//        return result;
//    }
//
//    // 시간대별 Redis 메모리 사용량 받기
//    public List<Map<String, Object>> getRedisMemoryLog() {
//        LocalDateTime start = LocalDate.now().atStartOfDay();
//        LocalDateTime end = LocalDate.now().plusDays(1).atStartOfDay();
//
//        log.info("start-0----> {}", start);
//        log.info("start-0----> {}", end);
//        // RedisStat 엔티티 리스트를 가져옵니다 (Redis에서 직접 조회)
//        List<RedisLog> logs = new ArrayList<>();
////        redisLogRepository.findAll().forEach(logs::add);// 모든 데이터를 가져온 후 필터링
//        log.info("logs-----> {}", logs);
//        List<Map<String, Object>> result = new ArrayList<>();
//
//        long count = logs.stream()
//                .filter(stat -> !stat.getTimestamp().isBefore(start) && stat.getTimestamp().isBefore(end))
//                .count();
//
//        log.info("count-----> {}", count);
//        logs.stream()
//                .filter(stat -> !stat.getTimestamp().isBefore(start) && stat.getTimestamp().isBefore(end)) // 조건에 맞는 데이터 필터링
//                .forEach(stat -> {
//                    Map<String, Object> entry = new HashMap<>();
//                    entry.put("time", stat.getTimestamp().toLocalTime().withMinute(0).withSecond(0).toString()); // "HH:00"
//                    entry.put("memoryUsageMb", stat.getMemoryUsageMb());
//                    result.add(entry);
//                });
//        return result;
//
//    }
//
//    public long countByTimestampBetween(LocalDateTime start, LocalDateTime end) {
//        Double startScore = (double) start.toEpochSecond(java.time.ZoneOffset.UTC);
//        Double endScore = (double) end.toEpochSecond(java.time.ZoneOffset.UTC);
//        Long count = redisTemplate.opsForZSet().count("RedisLog", startScore, endScore);
//        return count != null ? count : 0;
//    }
//
//
//    // RedisStat 데이터 저장 메서드
//    private class RedisLogService {
//
//        public void saveRedisUsage(double usage) {
//            RedisLog redisLog = new RedisLog();
//            redisLog.setTimestamp(LocalDateTime.now());
//            redisLog.setMemoryUsageMb(usage);
//
//            // 로그 출력
//            System.out.println("Redis 사용량 저장: " + usage + "MB");
//
//
//        }
//
//    }


}
