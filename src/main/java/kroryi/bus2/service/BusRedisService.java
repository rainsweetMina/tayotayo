package kroryi.bus2.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
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
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Log4j2
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class BusRedisService {

    private final RestTemplate restTemplate;
    private final FakeRedis fakeRedis;
    private final BusApiService busApiService;
    private final long CACHE_EXPIRATION = 60;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    private final BusStopRepository busStopRepository;
    private final ObjectMapper objectMapper;


    //   Redis 관련
    public String getBusArrival(String busStopId) {
        // Redis에서 캐싱된 데이터 가져오기
        String key = "busArrival:" + busStopId;
        String cachedData = (String) redisTemplate.opsForValue().get(key);

        if (cachedData != null) {
            System.out.println("Redis에서 데이터 가져옴");
            return cachedData;
        }

        // Redis에 캐싱된 데이터가 없으면 API 에서 가져오기
        System.out.println("API에서 데이터 가져옴");
        String response = busApiService.getBusArrivalInfo(busStopId);

        // Redis에 TTL 60초로 저장 (자동 갱신X)
        redisTemplate.opsForValue().set(key, response, 60, TimeUnit.SECONDS);

        return response;

    }

    public void loadBusStopsToRedis() {
        List<BusStop> busStops = busStopRepository.findAll();
        System.out.println("버스 정류장 갯수: " + busStops.size());
        for (BusStop stop : busStops) {
            // Redis에 값 저장
            try {
                redisTemplate.opsForValue().set("bus_stop:" + stop.getId(), stop);
                System.out.println("Redis 저장 성공 - Key: bus_stop:" + stop.getId());
            } catch (Exception e) {
                System.out.println("Redis 저장 실패- 이유: " + e.getMessage());
            }
        }
    }
    }

//    Redis 설정 끝




