package kroryi.bus2.controller;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import kroryi.bus2.dto.BusRealtimeDTO;
import kroryi.bus2.dto.busStop.BusStopDTO;
import kroryi.bus2.dto.coordinate.CoordinateDTO;
import kroryi.bus2.entity.BusStop;
import kroryi.bus2.entity.CustomRoute;
import kroryi.bus2.entity.Route;
import kroryi.bus2.repository.jpa.BusStopRepository;
import kroryi.bus2.service.BusInfoInitService;
import kroryi.bus2.service.BusStopDataService;
import kroryi.bus2.service.CustomeRoute.GetCustomRouteService;
import kroryi.bus2.service.RouteDataService;
import kroryi.bus2.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;

@RestController
@RequestMapping("/api/bus")
@RequiredArgsConstructor
@Log4j2
// 버스 관련 데이터를 클라이언트에 제공하는 REST API 컨트롤러
// 이 컨트롤러는 JSON 형식으로 데이터를 반환하며, 클라이언트(웹/앱)에서 실시간 정보 조회 및 검색에 활용
public class BusDataController {
    private final BusInfoInitService busInfoInitService;
    private final BusStopDataService busStopDataService;
    private final RouteDataService routeDataService;
    private final ObjectMapper objectMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final BusRouteRealTimeDataService busRouteRealTimeDataService;
    private final GetCustomRouteService getCoordinatesByRouteIdGrouped;
    private final GetCustomRouteService getCustomRouteService;
    private final BusStopRepository busStopRepository;

    @Value("${api.service-key-decoding}")
    private String serviceKey;


    // 전체 버스정류장 불러오는거, 데이터가 너무 많아서 5개만 불러옴 이젠 안씀 추후 삭제 예정
    @Operation(summary = "5개의 정류장 불러오기", description = "전체 버스정류장 불러오는거, 데이터가 너무 많아서 5개만 불러옴 이젠 안씀 추후 삭제 예정")
    @GetMapping(value = "/busStops", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<BusStopDTO>> getBusStop() throws JsonProcessingException {

        List<BusStopDTO> list = busStopDataService.getAllBusStops();
        log.info("데이터 : {}", objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(list));
        return ResponseEntity.ok(list);
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
    public ResponseEntity<?> searchBSOrBN(@RequestParam String keyword) throws JsonProcessingException {


        System.out.println("검색어 : " + keyword);

        List<BusStop> busStop = busStopDataService.getBusStopsByNm(keyword);

        System.out.println("-----------------------------------");

        List<Route> busNumber = routeDataService.getBusByNm(keyword);

        List<CustomRoute> CustomBusNumber = routeDataService.getCustomBusByNm(keyword);

        Map<String, Object> response = new HashMap<>();
        response.put("busStops", busStop);
        response.put("busNumbers", busNumber);
        response.put("CustomBusNumber", CustomBusNumber);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "노선Id로 정류장 불러오기", description = "노선Id로 해당하는 정류장 정보(좌표,이름 등)을 뿌려줌")
    @GetMapping(value = "/bus-route", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Map<String, Object>> getBusRoute(@RequestParam String routeId) throws IOException {
//        JsonNode result = routeDataService.getBusRoute(routeId);
        List<Map<String, Object>> result = getCustomRouteService.getBusRoute(routeId);

        return ResponseEntity.ok(result).getBody();
    }

    //     ORS 활용한 api 지도에 노선 그리는거
    @Operation(summary = "노선Id로 경로 불러오기", description = "노선Id로 해당하는 노선의 경로의 좌표 값을 뿌려줌 (ORS 활용)")
    @GetMapping("/bus-route-link")
    public ResponseEntity<Map<String, List<CoordinateDTO>>> getBusRouteLinkWithCoordsORS(@RequestParam String routeId) throws IOException, InterruptedException {
        String redisKey = "bus:route:ors:" + routeId;

        Map<String, List<CoordinateDTO>> cached = (Map<String, List<CoordinateDTO>>) redisTemplate.opsForValue().get(redisKey);
        if (cached != null) {
            return ResponseEntity.ok(cached);
        }

        Map<String, List<CoordinateDTO>> resultMap = routeDataService.getOrsRouteByBusDirection(routeId);
        redisTemplate.opsForValue().set(redisKey, resultMap, Duration.ofDays(1)); // 1일 TTL

        return ResponseEntity.ok(resultMap);
    }

    @Operation(summary = "노선Id로 Custom 정류장 불러오기 ", description = "노선Id로 해당하는 Custom 정류장 정보(좌표,이름 등)을 뿌려줌")
    @GetMapping(value = "/bus-route-Custom", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Map<String, Object>> getCustomBusRoute(@RequestParam String routeId) throws IOException {

        List<Map<String, Object>> result = getCustomRouteService.getBusRoute(routeId);
        System.out.println("커스텀 버스 노선 : " + result);

        return ResponseEntity.ok(result).getBody();
    }

    @Operation(summary = "노선Id로 Custom 경로 불러오기", description = "노선Id로 해당하는 Custom 노선의 경로의 좌표 값을 뿌려줌 (ORS 활용)")
    @GetMapping("/bus-route-link-Custom")
    public ResponseEntity<Map<String, List<CoordinateDTO>>> getCustomBusRouteLinkWithCoordsORS(@RequestParam String routeId) throws IOException, InterruptedException {
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
        Map<String, List<CoordinateDTO>> rawMap = getCoordinatesByRouteIdGrouped.getCoordinatesByRouteIdGrouped(routeId);
        System.out.println("rawMap : " + rawMap);

        // 3. ORS 경로 처리
        List<CoordinateDTO> forwardPath = getCoordinatesByRouteIdGrouped.getChunkedOrsCustom(rawMap.getOrDefault("forward", List.of()));
        List<CoordinateDTO> reversePath = getCoordinatesByRouteIdGrouped.getChunkedOrsCustom(rawMap.getOrDefault("reverse", List.of()));

        Map<String, List<CoordinateDTO>> resultMap = new HashMap<>();
        resultMap.put("forward", forwardPath);
        resultMap.put("reverse", reversePath);

        System.out.println("resultMap : " + resultMap);

        // 4. 캐시 저장 (10분)
        redisTemplate.opsForValue().set(redisKey, objectMapper.writeValueAsString(resultMap), Duration.ofMinutes(60));
        System.out.println("📝 Redis 캐시에 저장 완료: " + redisKey);

        return ResponseEntity.ok(resultMap);
    }

    @Operation(summary = "노선Id로 버스 실시간 위치", description = "노선Id로 해당하는 노선에 다니고 있는 버스의 실시간 위치를 뿌려줌")
    @GetMapping("/bus-route-Bus")
    public ResponseEntity<List<BusRealtimeDTO>> getBusRouteRealTimeBus(@RequestParam String routeId) throws Exception {

        List<BusRealtimeDTO> list = busRouteRealTimeDataService.getRealTimeBusList(routeId);
        System.out.println("버스 실시간 위치 결과 : " + list);

        return ResponseEntity.ok(list);
    }


    @GetMapping("/stop-name")
    public ResponseEntity<String> getBusStopName(@RequestParam String bsId) {
        return busStopRepository.findByBsId(bsId)
                .map(busStop -> ResponseEntity.ok(busStop.getBsNm()))
                .orElse(ResponseEntity.notFound().build());

    }





    // 레디스 수동으로 지우는컨트롤러     조심히 다루세요
    @Operation(summary = "Redis 전체 캐시 삭제", description = "Redis에 저장된 모든 캐시 데이터를 삭제합니다. 운영 환경에서는 주의해서 사용하세요.")
    @DeleteMapping("/evict/all")
    public ResponseEntity<String> evictAllCache() {
        Set<String> keys = redisTemplate.keys("*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
            log.info("[Redis] 전체 캐시 삭제됨. 삭제된 키 수: {}", keys.size());
            log.info("[Redis] 삭제된 키 목록:\n{}", String.join("\n", keys));
            return ResponseEntity.ok("모든 Redis 캐시가 삭제되었습니다. 삭제된 키 수: " + keys.size());
        } else {
            log.info("[Redis] 삭제할 캐시가 없습니다.");
            return ResponseEntity.ok("삭제할 캐시가 없습니다.");
        }
    }


    // 얘는 db에 기초종합정보 넣는거 이젠 쓰지마시길 렉 걸림 (나중에 하루에 한번 자동으로 실행되어 데이터 갱싱용으로 바꿀 예정)
    @Operation(summary = "공공데이터 기초 종합 정보", description = "얘는 db에 기초종합정보 넣는거 이젠 쓰지마시길 렉 걸림 (나중에 하루에 한번 자동으로 실행되어 데이터 갱싱용으로 바꿀 예정)")
    @PostMapping("/fetch")
    public String fetchPostBusData() {
//        String routeId = "1000001000";
        String API_URL = "https://apis.data.go.kr/6270000/dbmsapi01/getBasic?";
        String apiUrl = API_URL + "serviceKey=" + URLEncoder.encode(serviceKey, StandardCharsets.UTF_8);

        busInfoInitService.fetchAndSaveBusData(apiUrl);
        return "Bus data fetched and saved successfully!";
    }
}
