package kroryi.bus2.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import kroryi.bus2.entity.RedisStat;
import kroryi.bus2.repository.jpa.BusStopRepository;
import kroryi.bus2.repository.redis.RedisStatRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;


import java.time.LocalDateTime;
import java.util.Properties;

@Service
@Log4j2
@Transactional
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
// DB에 저장된 버스 정류장 정보를 Redis에 초기화 및 캐싱하는 기능을 담당하는 서비스 클래스
public class BusRedisService {


    // Redis 에 저장 관련 코드
    private final RedisStatRepository redisStatRepository;
    private final RedisConnectionFactory redisConnectionFactory;


    // Redis 사용량을 저장하는 메서드
    public void saveRedisUsage() {
        try (var connection = redisConnectionFactory.getConnection()) {

            // 메모리 사용량을 가져옴
            Properties memoryInfo = connection.info("memory");
            String memoryUsageStr = memoryInfo.getProperty("used_memory");

            if (memoryUsageStr == null) {
                System.out.println("Redis 메모리 사용량을 가져오지 못했습니다.");
                return;
            }


            // 메모리 사용량을 Long 으로 변환
            long memoryUsage;
            try {
                memoryUsage = Long.parseLong(memoryUsageStr);
            } catch (NumberFormatException e) {
                System.out.println("Redis 메모리 사용량 변환 오류: " + memoryUsageStr);
                return;
            }

            //  메모리 사용량을 MB로 변환하여 저장
            double usageMb = memoryUsage / 1024.0 / 1024.0;
            RedisStat redisStat = new RedisStat();
            redisStat.setTimestamp(LocalDateTime.now());
            redisStat.setMemoryUsageMb(usageMb);

            log.info("-----------------");
            log.info(redisStat.toString());

            redisStatRepository.save(redisStat);
            System.out.println("Redis 사용량 저장: " + memoryUsage + " MB");

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


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



