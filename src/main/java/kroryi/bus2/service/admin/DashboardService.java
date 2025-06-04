package kroryi.bus2.service.admin;

import kroryi.bus2.dto.RedisStats;
import kroryi.bus2.dto.lost.LostStatResponseDTO;
import kroryi.bus2.repository.jpa.FoundItemRepository;
import kroryi.bus2.repository.jpa.LostFoundMatchRepository;
import kroryi.bus2.repository.jpa.LostItemRepository;
import kroryi.bus2.repository.jpa.route.RouteRepository;
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
    private final RedisLogService redisLogService;

    // 🔵 분실물 통계 메서드 시작
    public LostStatResponseDTO getLostStats() {
        long reported = lostItemRepository.count();
        long found = foundItemRepository.count();
        long matched = lostFoundMatchRepository.count();

        return new LostStatResponseDTO(reported, found, matched);
    }
    // 🔵 분실물 통계 메서드 종료

    // 대시보드 통계 데이터 수집
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> result = new HashMap<>();

        long routeCount = routeRepository.count();

        // Redis 상태 정보 가져오기
        RedisStats redisStats = redisLogService.getRedisInfo();

        // Redis 정보가 정상적으로 수집된 경우에만 처리
        if (redisStats.getError() == null) {
            result.put("redisUsedMemory", String.valueOf(redisStats.getUsedMemory()));
            result.put("redisMaxMemory", String.valueOf(redisStats.getMaxMemory()));
            result.put("redisConnectedClients", String.valueOf(redisStats.getConnectedClients()));
            result.put("redisUptime", redisStats.getUptime());
            result.put("redisVersion", redisStats.getVersion());
        } else {
            // Redis 에러가 있는 경우 에러 정보 포함
            result.put("redisError", redisStats.getError());
            result.put("redisUsedMemory", "0");
            result.put("redisMaxMemory", "0");
            result.put("redisConnectedClients", "0");
            result.put("redisUptime", "0");
            result.put("redisVersion", "unknown");
        }

        result.put("routesCount", routeCount);

        log.info("📊 대시보드 통계 데이터 수집 완료: {}", result);
        return result;
    }
}