package kroryi.bus2.service;

import kroryi.bus2.dto.busStopDTO.BusStopDTO;
import kroryi.bus2.entity.BusStop;
import kroryi.bus2.repository.jpa.BusStopRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Log4j2
// 버스 정류장 관련 기능의 서비스 클래스
public class BusStopDataService {

    private final BusStopRepository busStopRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final BusArrivalService busArrivalService;


    public List<BusStopDTO> getAllBusStops() {
        System.out.print("서비스 응답");
        PageRequest pageRequest = PageRequest.of(0, 5);
//        System.out.printf(busStopRepository.findBusStops(pageRequest).toString());
        return busStopRepository.findBusStops(pageRequest).stream()
                .map(busStop -> BusStopDTO.builder()
                        .bsId(busStop.getBsId())
                        .bsNm(busStop.getBsNm())
                        .xPos(busStop.getXPos())
                        .yPos(busStop.getYPos())
                        .build()).collect(Collectors.toList());
    }


    private final long CACHE_EXPIRATION = 15;
    public String getRedisBusStop(String bsId) {
        // Redis에서 캐싱된 데이터 가져오기
        String key = "busArrival:" + bsId;
        String cachedData = (String) redisTemplate.opsForValue().get(key);

        if (cachedData != null) {
            System.out.println("Redis에서 데이터 가져옴");
            return cachedData;
        }

        System.out.println("Redis에서 데이터 없음 -> API 에서 호출");

        // API 호출 성공 확인
        String response = busArrivalService.getBusArrivalInfo(bsId);
        System.out.printf("response: %s\n", response);
        System.out.println("API에서 데이터 가져옴");

        redisTemplate.opsForValue().set(key, response, CACHE_EXPIRATION, TimeUnit.SECONDS);
        log.info("Redis에 데이터 저장 완료 - Key: {}", key);

        return response;
    }

    public List<BusStop> getBusStopsByNm(String nm) {
        String cacheKey = "busstop:nm:" + nm;
        System.out.println("🔍 검색 요청: " + nm);
        System.out.println("🔑 Redis 캐시 키: " + cacheKey);

        // 1. Redis 캐시 먼저 확인
        List<BusStop> cached = (List<BusStop>) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            System.out.println("✅ 캐시 히트! Redis에서 결과 가져옴 (결과 수: " + cached.size() + ")");
            return cached;
        } else {
            System.out.println("❌ 캐시 미스. Redis에 없음 → DB 조회로 진행");
        }

        // 2. DB에서 검색
        List<BusStop> result = busStopRepository.findByBsNmContaining(nm);
        if (result.isEmpty()) {
            System.out.println("🔎 부분 일치 결과 없음 → 공백 무시 검색 시도");
            result = busStopRepository.searchByBsNmIgnoreSpace(nm);
        } else {
            System.out.println("✅ 부분 일치 검색 성공 (결과 수: " + result.size() + ")");
        }

        // 3. 결과를 이름 기반 키로 캐싱
        if (!result.isEmpty()) {
            redisTemplate.opsForValue().set(cacheKey, result, 60, TimeUnit.MINUTES);
            System.out.println("🧊 결과 Redis에 캐싱 완료 (TTL: 1시간)");
        } else {
            System.out.println("⚠️ DB 검색 결과 없음. 캐싱 생략");
        }

        return result;
    }



}
