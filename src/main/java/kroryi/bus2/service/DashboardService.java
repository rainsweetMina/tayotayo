package kroryi.bus2.service;


import kroryi.bus2.dto.lost.LostStatResponseDTO;
import kroryi.bus2.entity.ApiLog;
import kroryi.bus2.repository.jpa.FoundItemRepository;
import kroryi.bus2.repository.jpa.LostFoundMatchRepository;
import kroryi.bus2.repository.jpa.LostItemRepository;
import kroryi.bus2.repository.redis.ApiLogRepository;
import kroryi.bus2.repository.jpa.route.RouteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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



}
