package kroryi.bus2.service;

import kroryi.bus2.dto.BusStopDTO;
import kroryi.bus2.dto.RouteDTO;
import kroryi.bus2.entity.BusStop;
import kroryi.bus2.entity.Route;
import kroryi.bus2.repository.BusStopRepository;
import kroryi.bus2.repository.RouteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.awt.print.Pageable;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BusStopDataService {

    private final BusStopRepository busStopRepository;
    private final RedisTemplate<String, Object> redisTemplate;

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
            redisTemplate.opsForValue().set(cacheKey, result, 10, TimeUnit.MINUTES);
            System.out.println("🧊 결과 Redis에 캐싱 완료 (TTL: 10분)");
        } else {
            System.out.println("⚠️ DB 검색 결과 없음. 캐싱 생략");
        }

        return result;

//        // 1. 기본 검색 (부분 검색)
//        List<BusStop> result = busStopRepository.findByBsNmContaining(nm);
//        // 2. 띄어쓰기 무시 검색
//        if (result.isEmpty()) {
//            result = busStopRepository.searchByBsNmIgnoreSpace(nm);
//        }

//        return result;
    }

}
