package kroryi.bus2.util;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;



//우선은 컴퓨터에 redis 관련 프로그램 미설치상태에서 실행하도록 해봤어요
//나중에 설치한버전으로 바꿀거에요
//실행하고 브라우저에 http://localhost:8081/api/bus/nav?bsId=7001001400 (bsId 필수!) 제대로 작동하는지 확인 가능해요
//혹시 몰라서 밑에 콘솔에도 띄어놨어요
@Component
public class FakeRedis {
    private final Map<String ,Object> cache = new ConcurrentHashMap<>();

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public void set(String key, Object value) {
        cache.put(key, value);
    }

    public Object get(String key) {
        Object value = cache.get(key);
        System.out.printf("get: %s\n", value);
        if (value != null) {
            System.out.println("✅ [FakeRedis] 캐시 HIT : " + key);
        } else {
            System.out.println("❌ [FakeRedis] 캐시 MISS : " + key);
        }
        return value;
    }

    public void setWithTTL(String key, Object value, long seconds) {
        cache.put(key, value);
        scheduler.schedule(() -> cache.remove(key), seconds, TimeUnit.SECONDS);
        System.out.printf("데이터 저장");
    }

    public void delete(String key) {
        cache.remove(key);
    }
}
