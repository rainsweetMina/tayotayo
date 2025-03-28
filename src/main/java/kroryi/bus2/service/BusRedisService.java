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
@RequiredArgsConstructor
// DB에 저장된 버스 정류장 정보를 Redis에 초기화 및 캐싱하는 기능을 담당하는 서비스 클래스
public class BusRedisService {

    private final RestTemplate restTemplate;
    private final BusArrivalService busArrivalService;

    private final BusStopRepository busStopRepository;
    private final ObjectMapper objectMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;


    // 이건 민경씨가 만들어준 서버 실행시 db에서 모든 버스정류장 정보를 가져와서 Redis에 넣는건데 현재는 쓰는곳이 없어서 일단 주석 처리 해뒀습니다.
    // ✅ @PostConstruct 추가 → 실행 시 자동 실행
//    @PostConstruct
//    public void init() {
//        log.info("🚀 Redis 초기화 시작");
//        loadBusStopsToRedis(); // 애플리케이션 실행 시 자동 실행
//    }

//    public void loadBusStopsToRedis() {
//        List<BusStop> busStops = busStopRepository.findAll();
//        System.out.println("버스 정류장 갯수: " + busStops.size());
//        System.out.println("버스 정류장1 : " + busStops.get(0));
//
//        boolean alreadyCached = false;
//
//        for (BusStop stop : busStops) {
//            String key = "bus_stop" + stop.getId();
//
//            // Redis 에 이미 값이 있는 경우 스킵
//            if (Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
//                if (!alreadyCached) {
//                    System.out.println("Redis에 이미 값이 존재합니다. 덮어쓰지 않습니다.");
//                    alreadyCached = true;
//                }
//                continue;
//            }
//            try {
//                redisTemplate.opsForValue().set(key, stop, 600, TimeUnit.SECONDS);
//                System.out.println("Redis 저장 성공 - Key:" + stop.getId());
//            } catch (Exception e) {
//                System.out.println("🚨 Redis 저장 실패 - 이유: " + e.getMessage());
//            }
//
//
//        }
//    }


//    Redis 설정 끝

}



