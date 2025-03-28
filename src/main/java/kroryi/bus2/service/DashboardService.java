package kroryi.bus2.service;

import kroryi.bus2.entity.ApiLog;
import kroryi.bus2.entity.RedisStat;
import kroryi.bus2.repository.ApiLogRepository;
import kroryi.bus2.repository.RedisStatRepository;
import kroryi.bus2.repository.RouteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final RouteRepository routeRepository;
    private final ApiLogRepository apiLogRepository;
    private final RedisStatRepository redisStatRepository;

    // 검색량, 길찾기 검색, 레디스 사용량 받아오기
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> result = new HashMap<>();

        long routeCount = routeRepository.count();
        long requestCountToday = apiLogRepository.countByTimestampBetween(
                LocalDate.now().atStartOfDay(),
                LocalDate.now().plusDays(1).atStartOfDay()
        );
        double redisUsage = redisStatRepository.findTopByOrderByTimestampDesc()
                .map(RedisStat::getMemoryUsageMb)
                .orElse(0.0);

        result.put("routesCount", routeCount);
        result.put("requestToday", requestCountToday);
        result.put("redisUsage", redisUsage + " MB") ;


        return result;
    }

    //  오늘 하루 동안 요청된 API 로그의 총 개수
    public List<Map<String, Object>> getApiSuccessStats() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);

        List<ApiLog> logs = apiLogRepository.findByTimestampBetween(startOfDay, endOfDay);

        Map<Integer, Integer> successMap = new HashMap<>();
        Map<Integer, Integer> failMap = new HashMap<>();

        // 초기화: 0~23시까지

        for (int i = 0; i < 24; i++) {
            successMap.put(i, 0);
            failMap.put(i, 0);
        }

        for (ApiLog log : logs) {
            int hour = log.getTimestamp().getHour();
            if (log.isSuccess()) {
                successMap.put(hour, successMap.getOrDefault(hour, 0) + 1);
            } else {
                failMap.put(hour, failMap.get(hour) + 1);
            }
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (int i = 0; i < 24; i++) {
            Map<String, Object> entry = new HashMap<>();
            entry.put("hour", String.format("%02d", i));
            entry.put("success", successMap.getOrDefault(i, 0));
            entry.put("fail", failMap.get(i));
            result.add(entry);
        }

        return result;

    }


    // 에러 로그 가져오기
    public List<Map<String, Object>> getRecentErrors() {
        List<ApiLog> errorLogs = apiLogRepository.findTop10BySuccessIsFalseOrderByTimestampDesc();
        List<Map<String, Object>> result = new ArrayList<>();

        for (ApiLog log : errorLogs) {
            Map<String, Object> entry = new HashMap<>();
            entry.put("timestamp", log.getTimestamp().toString());
            entry.put("errorMessage", log.getErrorMessage());
            result.add(entry);
        }

        return result;
    }




}
