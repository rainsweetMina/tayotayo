package kroryi.bus2.service;

import kroryi.bus2.entity.RedisStat;
import kroryi.bus2.repository.ApiLogRepository;
import kroryi.bus2.repository.RedisStatRepository;
import kroryi.bus2.repository.RouteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;


import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final RouteRepository routeRepository;
    private final ApiLogRepository apiLogRepository;
    private final RedisStatRepository redisStatRepository;

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



}
