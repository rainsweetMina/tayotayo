package kroryi.bus2.service.busStop;

import kroryi.bus2.dto.Route.RouteIdAndNoDTO;
import kroryi.bus2.dto.busStop.BusStopDTO;
import kroryi.bus2.dto.busStop.BusStopFullDetailDTO;
import kroryi.bus2.dto.busStop.BusStopListDTO;
import kroryi.bus2.entity.busStop.BusStop;
import kroryi.bus2.entity.busStop.BusStopInfo;
import kroryi.bus2.repository.jpa.board.BusStopInfoRepository;
import kroryi.bus2.repository.jpa.bus_stop.BusStopRepository;
import kroryi.bus2.repository.jpa.board.RouteStopLinkRepository;
import kroryi.bus2.repository.jpa.route.RouteRepository;
import kroryi.bus2.service.bus.BusArrivalService;
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
    private final BusStopInfoRepository busStopInfoRepository;

    // 페이징과 검색이 적용된 전체 정류장 리스트 서비스
    public Page<BusStopListDTO> getBusStopsWithPaging(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("bsId").ascending());
        Page<BusStop> result = busStopRepository.findByKeyword(keyword, pageable);

        return result.map(stop -> BusStopListDTO.builder()
                .id(stop.getId())
                .bsId(stop.getBsId())
                .bsNm(stop.getBsNm())
                .xPos(stop.getXPos())
                .yPos(stop.getYPos())
                .distance(null)
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

    // 정류장의 모든 정보 + 오는 노선조회 합친거
    public BusStopFullDetailDTO getFullBusStopDetail(String bsId) {
        BusStop busStop = busStopRepository.findByBsId(bsId)
                .orElseThrow(() -> new IllegalArgumentException("정류장을 찾을 수 없습니다: " + bsId));

        BusStopInfo info = busStopInfoRepository.findByBsId(bsId); // 시군동 정보

        List<String> routeIds = routeStopLinkRepository.findRouteIdsByBusStopId(bsId);
        List<RouteIdAndNoDTO> routes = routeIds.isEmpty() ?
                Collections.emptyList() : routeRepository.findRoutesByIds(routeIds);

        return BusStopFullDetailDTO.builder()
                .bsId(busStop.getBsId())
                .bsNm(busStop.getBsNm())
                .xPos(busStop.getXPos())
                .yPos(busStop.getYPos())
                .city(info != null ? info.getCity() : null)
                .district(info != null ? info.getDistrict() : null)
                .neighborhood(info != null ? info.getNeighborhood() : null)
                .routes(routes)
                .build();
    }

    // 정류장에 오는 노선만 조회하는거
    public List<RouteIdAndNoDTO> getRoutesByBusStop(String bsId) {
        List<String> routeIds = routeStopLinkRepository.findRouteIdsByBusStopId(bsId);
        if (routeIds.isEmpty()) {
            return Collections.emptyList();
        }
        return routeRepository.findRoutesByIds(routeIds);
    }

    // 반경 내 정류장 검색 (위도/경도 기준)
    public List<BusStopListDTO> findNearbyBusStops(double lon, double lat, int radius) {
        log.info("반경 {}m 내 정류장 검색: 좌표({}, {})", radius, lon, lat);

        // 데이터베이스에서 반경 내 정류장 검색
        List<BusStop> nearbyStops = busStopRepository.findStopsWithinRadius(lon, lat, radius);

        // 결과가 없으면 반경을 확장하여 재검색
        if (nearbyStops.isEmpty() && radius < 1000) {
            log.info("반경 {}m 내 정류장이 없어 반경 확장: 1000m", radius);
            nearbyStops = busStopRepository.findStopsWithinRadius(lon, lat, 1000);
        }

        // 여전히 결과가 없으면 가장 가까운 정류장 10개 검색
        if (nearbyStops.isEmpty()) {
            log.info("반경 1000m 내에도 정류장이 없어 가장 가까운 정류장 10개 검색");
            // 이 부분은 MySQL 공간 함수를 사용하여 구현 필요
            // 현재 구현에서는 간단히 모든 정류장을 가져와 거리 계산 후 정렬
            List<BusStop> allStops = busStopRepository.findAll();
            return allStops.stream()
                    .map(stop -> {
                        double distance = calculateDistance(lon, lat, stop.getXPos(), stop.getYPos());
                        return new BusStopListDTO(
                                stop.getId(),
                                stop.getBsId(),
                                stop.getBsNm(),
                                stop.getXPos(),
                                stop.getYPos(),
                                distance
                        );
                    })
                    .sorted((a, b) -> {
                        Double distanceA = a.getDistance();
                        Double distanceB = b.getDistance();
                        if (distanceA == null && distanceB == null) return 0;
                        if (distanceA == null) return 1;
                        if (distanceB == null) return -1;
                        return Double.compare(distanceA, distanceB);
                    })
                    .limit(10)
                    .collect(Collectors.toList());
        }

        // 검색된 정류장을 DTO로 변환
        return nearbyStops.stream()
                .map(stop -> {
                    double distance = calculateDistance(lon, lat, stop.getXPos(), stop.getYPos());
                    return new BusStopListDTO(
                            stop.getId(),
                            stop.getBsId(),
                            stop.getBsNm(),
                            stop.getXPos(),
                            stop.getYPos(),
                            distance
                    );
                })
                .collect(Collectors.toList());
    }

    // Haversine 공식을 사용한 두 지점 간의 거리 계산 (미터 단위)
    private double calculateDistance(double lon1, double lat1, double lon2, double lat2) {
        final int R = 6371; // 지구의 반지름 (km)

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c * 1000; // 미터 단위로 변환
    }

    // 지도 영역 내 정류장 검색 (좌표 범위 기준)
    public List<BusStopListDTO> findBusStopsInBounds(double minX, double minY, double maxX, double maxY) {
        log.info("지도 영역 내 정류장 검색: 좌표 범위 ({}, {}) ~ ({}, {})", minX, minY, maxX, maxY);

        // 데이터베이스에서 좌표 범위 내 정류장 검색
        List<BusStop> stopsInBounds = busStopRepository.findStopsInBounds(minX, minY, maxX, maxY);

        log.info("지도 영역 내 정류장 검색 결과: {}개", stopsInBounds.size());

        // 검색된 정류장을 DTO로 변환
        return stopsInBounds.stream()
                .map(stop -> new BusStopListDTO(
                        stop.getId(),
                        stop.getBsId(),
                        stop.getBsNm(),
                        stop.getXPos(),
                        stop.getYPos(),
                        0.0 // 거리는 계산하지 않음 (영역 내 검색이므로)
                ))
                .collect(Collectors.toList());
    }

}
