package kroryi.bus2.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import jakarta.annotation.PostConstruct;
import kroryi.bus2.entity.BusStop;
import kroryi.bus2.repository.BusStopRepository;
import kroryi.bus2.util.FakeRedis;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;


import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Log4j2
@RequiredArgsConstructor // final 필드들에 대해 자동으로 생성자를 만들어줌
public class BusRedisService {

    private final RestTemplate restTemplate;
    private final BusApiService busApiService;

    private final BusStopRepository busStopRepository;
    private final ObjectMapper objectMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private final long CACHE_EXPIRATION = 60;

//    private final FakeRedis fakeRedis;

    // ✅ @PostConstruct 추가 → 실행 시 자동 실행
    @PostConstruct
    public void init() {
        log.info("🚀 Redis 초기화 시작");
        loadBusStopsToRedis(); // 애플리케이션 실행 시 자동 실행
    }

    public String getBusArrival(String bsId) {
        // Redis에서 캐싱된 데이터 가져오기
        String key = "busArrival:" + bsId;
        String cachedData = (String) redisTemplate.opsForValue().get(key);

        if (cachedData != null) {
            System.out.println("Redis에서 데이터 가져옴");
            return cachedData;
        }

        System.out.println("Redis에서 데이터 없음 -> API 에서 호출");

        // API 호출 성공 확인
        String response = busApiService.getBusArrivalInfo(bsId);
        System.out.printf("response: %s\n", response);
        System.out.println("API에서 데이터 가져옴");

        redisTemplate.opsForValue().set(key, response, CACHE_EXPIRATION, TimeUnit.SECONDS);
        log.info("Redis에 데이터 저장 완료 - Key: {}", key);

        return response;
    }

    public void loadBusStopsToRedis() {
        List<BusStop> busStops = busStopRepository.findAll();
        System.out.println("버스 정류장 갯수: " + busStops.size());
        System.out.println("버스 정류장1 : " + busStops.get(0));

        boolean alreadyCached = false;

        for (BusStop stop : busStops) {
            String key = "bus_stop" + stop.getId();

            // Redis 에 이미 값이 있는 경우 스킵
            if (Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
                if (!alreadyCached) {
                    System.out.println("Redis에 이미 값이 존재합니다. 덮어쓰지 않습니다.");
                    alreadyCached = true;
                }
                continue;
            }
            try {
                redisTemplate.opsForValue().set(key, stop, 600, TimeUnit.SECONDS);
                System.out.println("Redis 저장 성공 - Key:" + stop.getId());
            } catch (Exception e) {
                System.out.println("🚨 Redis 저장 실패 - 이유: " + e.getMessage());
            }


        }
    }
}

//    Redis 설정 끝




