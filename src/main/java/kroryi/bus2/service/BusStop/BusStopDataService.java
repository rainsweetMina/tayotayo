package kroryi.bus2.service.BusStop;

import kroryi.bus2.dto.Route.RouteIdAndNoDTO;
import kroryi.bus2.dto.busStop.BusStopDTO;
import kroryi.bus2.dto.busStop.BusStopListDTO;
import kroryi.bus2.entity.bus_stop.BusStop;
import kroryi.bus2.repository.jpa.BusStopRepository;
import kroryi.bus2.repository.jpa.board.RouteStopLinkRepository;
import kroryi.bus2.repository.jpa.route.RouteRepository;
import kroryi.bus2.service.BusArrivalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
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
    private final RouteStopLinkRepository routeStopLinkRepository;
    private final RouteRepository routeRepository;

    // 페이징과 검색이 적용된 전체 정류장 리스트 서비스
    public Page<BusStopListDTO> getBusStopsWithPaging(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("bsId").ascending());
        Page<BusStop> result = busStopRepository.findByKeyword(keyword, pageable);

        return result.map(stop -> BusStopListDTO.builder()
                .id(stop.getId())
                .bsId(stop.getBsId())
                .bsNm(stop.getBsNm())
                .xpos(stop.getXPos())
                .ypos(stop.getYPos())
                .build());
    }


    public List<BusStopDTO> getAllBusStops() {
        System.out.print("서비스 응답");
//        PageRequest pageRequest = PageRequest.of(0, 5);
//        System.out.printf(busStopRepository.findBusStops(pageRequest).toString());
        return busStopRepository.findBusStops().stream()
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
        System.out.println("🔍 검색 요청: " + nm);

        // 1. DB에서 부분 일치 검색
        List<BusStop> result = busStopRepository.findByBsNmContaining(nm);
        if (result.isEmpty()) {
            System.out.println("🔎 부분 일치 결과 없음 → 공백 무시 검색 시도");
            result = busStopRepository.searchByBsNmIgnoreSpace(nm);
        } else {
            System.out.println("✅ 부분 일치 검색 성공 (결과 수: " + result.size() + ")");
        }

        return result;
    }

    public BusStop getBusStopById(String bsId) {

        return busStopRepository.findByBsId(bsId)
                .orElseThrow(() -> new IllegalArgumentException("정류장을 찾을 수 없습니다: " + bsId));
    }

    public List<RouteIdAndNoDTO> getRoutesByBusStop(String bsId) {
        List<String> routeIds = routeStopLinkRepository.findRouteIdsByBusStopId(bsId);
        if (routeIds.isEmpty()) {
            return Collections.emptyList();
        }
        return routeRepository.findRoutesByIds(routeIds);
    }


}
