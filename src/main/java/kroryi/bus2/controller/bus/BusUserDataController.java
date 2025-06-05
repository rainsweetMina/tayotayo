package kroryi.bus2.controller.bus;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import kroryi.bus2.dto.BusRealtimeDTO;
import kroryi.bus2.dto.Route.CustomRouteRegisterRequestDTO;
import kroryi.bus2.dto.Route.RouteDTO;
import kroryi.bus2.dto.Route.RouteListDTO;
import kroryi.bus2.dto.Route.RouteResultDTO;
import kroryi.bus2.dto.RouteStopLinkDTO;
import kroryi.bus2.dto.busStop.BusStopDTO;
import kroryi.bus2.dto.coordinate.CoordinateDTO;
import kroryi.bus2.dto.coordinate.CoordinateListWrapperDTO;
import kroryi.bus2.entity.busStop.BusStop;
import kroryi.bus2.entity.route.Route;
import kroryi.bus2.repository.jpa.board.RouteStopLinkRepository;
import kroryi.bus2.repository.jpa.bus_stop.BusStopRepository;
import kroryi.bus2.repository.jpa.route.RouteRepository;
import kroryi.bus2.service.BusInfoInitService;
import kroryi.bus2.service.BusRouteRealTimeDataService;
import kroryi.bus2.service.busStop.BusStopDataService;
import kroryi.bus2.service.route.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Tag(name = "사용자-버스-정보-서칭", description = "")
@RestController
@RequestMapping("/api/bus")
@RequiredArgsConstructor
@Log4j2
// 버스,노선 관련 데이터를 클라이언트에 제공하는 REST API 컨트롤러
// 이 컨트롤러는 JSON 형식으로 데이터를 반환하며, 클라이언트(웹/앱)에서 실시간 정보 조회 및 검색에 활용
public class BusUserDataController {
    private final BusStopDataService busStopDataService;
    private final RouteDataService routeDataService;
    private final ObjectMapper objectMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final BusRouteRealTimeDataService busRouteRealTimeDataService;
    private final GetRouteLinkService getRouteLinkService;
    private final BusStopRepository busStopRepository;
    private final RouteFinderService routeFinderService;


    @Value("${api.service-key-decoding}")
    private String serviceKey;

    @Value("${ors.api.key}")
    private String orsApiKey;

    @Operation(summary = "좌표기반 정류소 서칭", description = "전체 버스정류장을 좌표기반으로 불러오는거")
    @GetMapping("/busStopsInBounds")
    public ResponseEntity<List<BusStopDTO>> getBusStopsInBounds(
            @RequestParam double minX,
            @RequestParam double minY,
            @RequestParam double maxX,
            @RequestParam double maxY
    ) {
        List<BusStopDTO> stops = busStopRepository.findInBounds(minX, maxX, minY, maxY);
        return ResponseEntity.ok(stops);
    }

    // 이건 웹에서 정류장 클릭하면 해당 정류장의 버스 도착 정보 날려주는거
    // @param bsId 정류장 ID
    // @return 해당 정류장의 도착 예정 버스 정보 (JSON 형식)
    @Operation(summary = "정류장의 버스 도착 정보", description = "정류장 클릭하면 해당 정류장의 버스 도착 정보를 뿌려줌")
    @GetMapping("/bus-arrival")
    public ResponseEntity<JsonNode> getBusArrival(@RequestParam String bsId) throws JsonProcessingException {
        String jsonString = busStopDataService.getRedisBusStop(bsId);
        ObjectMapper mapper = new ObjectMapper();

        return ResponseEntity.ok(mapper.readTree(jsonString));
    }

    // 사용자가 검색창에 키워드를 입력했을 때, 해당 키워드에 해당하는 정류장명 또는 버스 노선명을 검색하여 반환
    // @param request { "keyword": "검색어" }
    // @return 정류장 목록과 버스 노선 번호 리스트를 포함한 JSON 응답
    @Operation(summary = "노선, 정류장 검색", description = "사용자가 검색창에 키워드를 입력했을 때, 해당 키워드에 해당하는 정류장명 또는 버스 노선명을 검색하여 반환")
    @GetMapping(value = "/searchBSorBN", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> searchBSOrBN(@RequestParam String keyword) {
        System.out.println("검색어 : " + keyword);
        List<BusStop> busStop = busStopDataService.getBusStopsByNm(keyword);
        System.out.println("-----------------------------------");
        List<Route> busNumber = routeDataService.getBusByNm(keyword);
        Map<String, Object> response = new HashMap<>();
        response.put("busStops", busStop);
        response.put("busNumbers", busNumber);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "노선이 경유하는 정류장 불러오기", description = "노선Id로 해당하는 정류장 정보(좌표,이름 등)을 뿌려줌",
            responses = {
                    @ApiResponse(responseCode = "200", description = "성공적으로 데이터 반환"),
                    @ApiResponse(responseCode = "401", description = "JWT 인증 실패"),
                    @ApiResponse(responseCode = "500", description = "서버 내부 오류")
            })
    @GetMapping(value = "/bus-route", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Map<String, Object>> getBusRoute(@RequestParam String routeId) throws IOException {
        List<Map<String, Object>> result = getRouteLinkService.getBusRoute(routeId);

        return ResponseEntity.ok(result).getBody();
    }

    @Operation(summary = "노선 경로 불러오기", description = "노선Id로 해당하는 노선의 경로의 좌표 값을 뿌려줌 (ORS 활용)")
    @GetMapping("/bus-route-link")
    public ResponseEntity<Map<String, List<CoordinateDTO>>> getBusRouteLinkWithCoordsORS(@RequestParam String routeId) throws IOException, InterruptedException {
        String redisKey = "BUS_ROUTE_POLYLINE::" + routeId;

        // 1. 캐시 확인
        String cached = (String) redisTemplate.opsForValue().get(redisKey);
        if (cached != null) {
            System.out.println("✅ Redis 캐시에서 결과 반환됨: " + redisKey);
            Map<String, List<CoordinateDTO>> cachedResult = objectMapper.readValue(
                    cached, new TypeReference<Map<String, List<CoordinateDTO>>>() {});
            return ResponseEntity.ok(cachedResult);
        }

        // 2. DB에서 좌표 가져오기
        Map<String, List<CoordinateDTO>> rawMap = getRouteLinkService.getCoordinatesByRouteIdGrouped(routeId);
        System.out.println("rawMap : " + rawMap);

        // 3. ORS 경로 처리
        List<CoordinateDTO> forwardPath = getRouteLinkService.getChunkedOrs(rawMap.getOrDefault("forward", List.of()));
        List<CoordinateDTO> reversePath = getRouteLinkService.getChunkedOrs(rawMap.getOrDefault("reverse", List.of()));

        Map<String, List<CoordinateDTO>> resultMap = new HashMap<>();
        resultMap.put("forward", forwardPath);
        resultMap.put("reverse", reversePath);

        System.out.println("resultMap : " + resultMap);

        // 4. 캐시 저장 (10분)
        redisTemplate.opsForValue().set(redisKey, objectMapper.writeValueAsString(resultMap), Duration.ofMinutes(60));
        System.out.println("📝 Redis 캐시에 저장 완료: " + redisKey);

        return ResponseEntity.ok(resultMap);
    }

    @Operation(summary = "버스 실시간 위치", description = "노선Id로 해당하는 노선에 다니고 있는 버스의 실시간 위치를 뿌려줌")
    @GetMapping("/bus-route-Bus")
    public ResponseEntity<List<BusRealtimeDTO>> getBusRouteRealTimeBus(@RequestParam String routeId) throws Exception {

        List<BusRealtimeDTO> list = busRouteRealTimeDataService.getRealTimeBusList(routeId);
        System.out.println("버스 실시간 위치 결과 : " + list);

        return ResponseEntity.ok(list);
    }

    @Operation(summary = "정류소이름 찾기", description = "정류소ID로 정류소 이름 찾아줌")
    @GetMapping("/stop-name")
    public ResponseEntity<String> getBusStopName(@RequestParam String bsId) {
        return busStopRepository.findByBsId(bsId)
                .map(busStop -> ResponseEntity.ok(busStop.getBsNm()))
                .orElse(ResponseEntity.notFound().build());

    }

    @Operation(summary = "길찾기", description = "출도착 정류소Id를 입력하면 직통,환승 된 노선을 찾아줌")
    @GetMapping("/findRoutes")
    public ResponseEntity<List<RouteResultDTO>> findRoutes(
            @RequestParam String startBsId,
            @RequestParam String endBsId) {

        String redisKey = "route:path:" + startBsId + ":" + endBsId;

        // 1. Redis 캐시 확인
        List<RouteResultDTO> cached = (List<RouteResultDTO>) redisTemplate.opsForValue().get(redisKey);
        if (cached != null) {
            log.info("✅ [Cache Hit] 길찾기 경로 Redis에서 가져옴: {}", redisKey);
            return ResponseEntity.ok(cached);
        }

        // 2. 없으면 서비스로 직접 계산
        List<RouteResultDTO> directResults = routeFinderService.findRoutesWithNearbyStart(startBsId, endBsId);
        List<RouteResultDTO> transferResults = routeFinderService.findRoutesWithNearbyStartTransfer(startBsId, endBsId);

        List<RouteResultDTO> combinedResults = new ArrayList<>();
        combinedResults.addAll(directResults);
        combinedResults.addAll(transferResults);

        // 3. Redis 캐시 저장 (TTL: 12시간 = 43200초)
        redisTemplate.opsForValue().set(redisKey, combinedResults, 43200, TimeUnit.SECONDS);
        log.info("[Cache Store] 길찾기 경로 Redis에 저장됨: {}", redisKey);

        return ResponseEntity.ok(combinedResults);
    }

    /**여기부터 Vue를 위한 api*/
    @Operation(summary = "좌표 기반 출발, 도착지 설정", description = "출발, 도착위치 핀 지정")
    @GetMapping("/nearby-stops")
    public ResponseEntity<Map<String, List<BusStopDTO>>> findNearbyStops(
            @RequestParam double startX,
            @RequestParam double startY,
            @RequestParam double endX,
            @RequestParam double endY,
            @RequestParam(defaultValue = "150") double radius
    ) {
        List<BusStop> startStops = busStopRepository.findStopsWithinRadius(startX, startY, radius);
        List<BusStop> endStops = busStopRepository.findStopsWithinRadius(endX, endY, radius);

        Map<String, List<BusStopDTO>> result = new HashMap<>();
        result.put("startCandidates", startStops.stream().map(BusStopDTO::fromEntity).toList());
        result.put("endCandidates", endStops.stream().map(BusStopDTO::fromEntity).toList());

        return ResponseEntity.ok(result);
    }

    @Operation(summary = "정류장 전용 검색", description = "검색어를 포함하는 정류장 이름만 반환 (노선 제외)")
    @GetMapping("/search-bus-stops")
    public ResponseEntity<List<BusStopDTO>> searchBusStops(@RequestParam String keyword) {
        List<BusStop> stops = busStopRepository.findByBsNmContaining(keyword);
        List<BusStopDTO> dtos = stops.stream()
                .map(BusStopDTO::fromEntity)
                .toList();

        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/ors/polyline")
    public ResponseEntity<?> getOrsPolyline(@RequestBody List<CoordinateDTO> coordinates) throws IOException, InterruptedException {
        log.info("📥 ORS 좌표 요청: {}개", coordinates.size());
        return ResponseEntity.ok(routeDataService.getOrsPath(coordinates));
    }

}
