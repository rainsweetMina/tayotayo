package kroryi.bus2.service.admin;

import kroryi.bus2.aop.AdminTracked;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;

@Service
public class DayStatsService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;


    // Redis에 저장할 키를 생성하는 메서드
    private String getTodayKey(String metric) {
        // "requests:20250407" 같은 키 만들기
        LocalDate today = LocalDate.now();
        return String.format("%s:%s", metric, today.toString().replace("-", ""));
    }

    // Redis에 저장된 키를 가져오는 메서드
    public void incrementRequestCount() {
        String key = getTodayKey("requests");
        Long count = redisTemplate.opsForValue().increment(key);
        // 최초 생성 시에만 TTL 설정 (24시간)
        if (count != null && count == 1L) {
            redisTemplate.expire(key, Duration.ofDays(1));
        }
        System.out.println("Generated key: " + key);
        System.out.println("Incremented count for key " + key + ": " + count);
    }

    public void incrementSearchCount() {
        String key = getTodayKey("searches");
        redisTemplate.opsForValue().increment(key);
    }

    public long getRequestCount() {
        String key = getTodayKey("requests");
        Object count = redisTemplate.opsForValue().get(key);
        if (count instanceof Integer) {
            return ((Integer) count).longValue();
        } else if (count instanceof Long) {
            return (Long) count;
        } else {
            return 0;
        }
    }

    public long getSearchCount() {
        String key = getTodayKey("searches");
        Object count = redisTemplate.opsForValue().get(key);
        return count == null ? 0 : (Long) count;
    }


}
