package kroryi.bus2.controller.api;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import kroryi.bus2.dto.BusRealtimeDTO;
import kroryi.bus2.dto.Route.CustomRouteRegisterRequestDTO;
import kroryi.bus2.dto.Route.RouteDTO;
import kroryi.bus2.dto.Route.RouteListDTO;
import kroryi.bus2.dto.Route.RouteResultDTO;
import kroryi.bus2.dto.RouteStopLinkDTO;
import kroryi.bus2.dto.busStop.BusStopDTO;
import kroryi.bus2.dto.coordinate.CoordinateDTO;

import kroryi.bus2.entity.busStop.BusStop;
import kroryi.bus2.entity.route.Route;
import kroryi.bus2.repository.jpa.bus_stop.BusStopRepository;

import kroryi.bus2.repository.jpa.board.RouteStopLinkRepository;
import kroryi.bus2.repository.jpa.route.RouteRepository;
import kroryi.bus2.service.BusInfoInitService;
import kroryi.bus2.service.busStop.BusStopDataService;
import kroryi.bus2.service.route.*;
import kroryi.bus2.service.route.RouteDataService;
import kroryi.bus2.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
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
// 버스,노선 관련 데이터를 클라이언트에 제공하는 REST API 컨트롤러
// 이 컨트롤러는 JSON 형식으로 데이터를 반환하며, 클라이언트(웹/앱)에서 실시간 정보 조회 및 검색에 활용
public class BusDataController {
    private final BusInfoInitService busInfoInitService;
    private final BusStopDataService busStopDataService;
    private final RouteDataService routeDataService;
    private final ObjectMapper objectMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final BusRouteRealTimeDataService busRouteRealTimeDataService;
    private final GetRouteLinkService getRouteLinkService;
    private final BusStopRepository busStopRepository;
    private final RouteRepository routeRepository;
    private final AddRouteService addRouteService;
    private final AddRouteStopLinkService addRouteStopLinkService;
    private final RouteStopLinkRepository routeStopLinkRepository;
    private final InsertStopIntoRouteService insertStopIntoRouteService;
    private final DeleteStopFromRouteService deleteStopFromRouteService;
    private final DeleteRouteService deleteRouteService;
    private final RouteFinderService routeFinderService;


    @Value("${api.service-key-decoding}")
    private String serviceKey;

    // 페이징 + 검색이 추가된 전체 노선 게시판
    @Operation(summary = "전체 노선 불러오기", description = "페이징 + 검색이 추가된 전체 노선 게시판")
    @GetMapping("/routes")
    public ResponseEntity<Page<RouteListDTO>> getRoutes(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "asc") String sort // 🔽 asc 또는 desc
    ) {
        Page<RouteListDTO> result = routeDataService.getRoutesWithPaging(keyword, page, size, sort);
        return ResponseEntity.ok(result);
    }


//    // 전체 버스정류장 불러오는거
//    @Operation(summary = "정류장 불러오기", description = "전체 버스정류장 불러오는거")
//    @GetMapping(value = "/busStops", produces = MediaType.APPLICATION_JSON_VALUE)
//    public ResponseEntity<List<BusStopDTO>> getBusStop() throws JsonProcessingException {
//
//        List<BusStopDTO> list = busStopDataService.getAllBusStops();
//        log.info("데이터 : {}", objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(list));
//        return ResponseEntity.ok(list);
//    }

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

    @Operation(summary = "정류장 불러오기", description = "노선Id로 해당하는 정류장 정보(좌표,이름 등)을 뿌려줌",
            responses = {
                    @ApiResponse(responseCode = "200", description = "성공적으로 데이터 반환"),
                    @ApiResponse(responseCode = "401", description = "JWT 인증 실패"),
                    @ApiResponse(responseCode = "500", description = "서버 내부 오류")
            })    @GetMapping(value = "/bus-route", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Map<String, Object>> getBusRoute(@RequestParam String routeId) throws IOException {
        List<Map<String, Object>> result = getRouteLinkService.getBusRoute(routeId);

        return ResponseEntity.ok(result).getBody();
    }

    //     ORS 활용한 api 지도에 노선 그리는거 근대 이거 불안정함 일단 사용x
//    @Operation(summary = "경로 불러오기", description = "노선Id로 해당하는 노선의 경로의 좌표 값을 뿌려줌 (ORS 활용) 근대 이거 불안정함 일단 사용x")
//    @GetMapping("/bus-route-link")
//    public ResponseEntity<Map<String, List<CoordinateDTO>>> getBusRouteLinkWithCoordsORS(@RequestParam String routeId) throws IOException, InterruptedException {
//        String redisKey = "bus:route:ors:" + routeId;
//
//        Map<String, List<CoordinateDTO>> cached = (Map<String, List<CoordinateDTO>>) redisTemplate.opsForValue().get(redisKey);
//        if (cached != null) {
//            return ResponseEntity.ok(cached);
//        }
//
//        Map<String, List<CoordinateDTO>> resultMap = routeDataService.getOrsRouteByBusDirection(routeId);
//        redisTemplate.opsForValue().set(redisKey, resultMap, Duration.ofDays(1)); // 1일 TTL
//
//        return ResponseEntity.ok(resultMap);
//    }


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

    // 경유 정류소만 추가 거의 쓸일없을듯?
    @Operation(summary = "경유지 추가", description = "새로운 경유 정류소만 추가 거의 쓸일없을듯?")
    @PostMapping("/AddRouteStopLink")
    public void addRouteStopLink(@RequestBody List<RouteStopLinkDTO> dtoList) {
        System.out.println("받아온 데이터 : " + dtoList);
        addRouteStopLinkService.saveAll(dtoList);
    }

    // 노선만들기 + 경유 정류소 추가
    @Operation(summary = "노선 경유지 추가", description = "새로운 노선 경유지를 추가합니다.")
    @PostMapping("/AddBusRoute")
    public ResponseEntity<?> addRoute(@RequestBody CustomRouteRegisterRequestDTO request) {
        try {
            addRouteService.saveFullRoute(request);
            return ResponseEntity.ok(Map.of("success", true, "routeId", request.getRoute().getRouteId()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @Operation(summary = "노선정보 찾기", description = "노선ID로 노선정보 찾아줌")
    @GetMapping("/getRouteInfo")
    public ResponseEntity<RouteDTO> getRouteByRouteId(@RequestParam String routeId) {
        RouteDTO route = routeDataService.getRouteByRouteId(routeId);
        return ResponseEntity.ok(route);
    }

    @Operation(summary = "노선정보 수정", description = "커스텀/일반 노선 구분 없이 노선ID로 정보 수정")
    @PutMapping("/UpdateRouteUnified/{routeId}")
    public ResponseEntity<?> updateAnyRoute(@PathVariable String routeId,
                                            @RequestBody RouteDTO updatedDto) {

        Optional<Route> normalOpt = routeRepository.findByRouteId(routeId);
        if (normalOpt.isPresent()) {
            Route route = normalOpt.get();
            route.setRouteNo(updatedDto.getRouteNo());
            route.setRouteNote(updatedDto.getRouteNote());
            route.setDataconnareacd(updatedDto.getDataconnareacd());
            route.setDirRouteNote(updatedDto.getDirRouteNote());
            route.setNdirRouteNote(updatedDto.getNdirRouteNote());
            route.setRouteTCd(updatedDto.getRouteTCd());

            routeRepository.save(route);
            return ResponseEntity.ok(Map.of("success", true, "message", "✅ 일반 노선 정보 수정 완료"));
        }

        // 둘 다 없으면 에러
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("success", false, "message", "해당 노선 ID를 찾을 수 없습니다."));
    }


    @Operation(summary = "노선링크 순서 수정", description = "노선ID로 노선링크의 순서를 수정해줌 *기존 노선엔 절대 사용금지!!!")
    @PutMapping("/UpdateRouteLink")
    public ResponseEntity<?> updateRouteSeq(@RequestBody List<RouteStopLinkDTO> dtoList) {
        if (dtoList.isEmpty()) {
            return ResponseEntity.badRequest().body("수정할 데이터가 없습니다.");
        }

        for (RouteStopLinkDTO dto : dtoList) {
            routeStopLinkRepository.findByRouteIdAndBsIdAndMoveDir(
                    dto.getRouteId(), dto.getBsId(), dto.getMoveDir()
            ).ifPresent(entity -> {
                entity.setSeq(dto.getSeq());
                routeStopLinkRepository.save(entity);
            });
        }

        return ResponseEntity.ok(Map.of("success", true, "message", "노선 순서(seq)가 성공적으로 수정되었습니다."));
    }

    @Operation(summary = "노선링크 정류소 추가", description = "노선ID로 노선링크의 정류소를 추가해줌 *기존의 노선에 새로운 정류소가 추가 될수도있으니 주의!")
    @PostMapping("/InsertStop")
    public ResponseEntity<?> insertStop(@RequestBody RouteStopLinkDTO dto) {
        try {
            insertStopIntoRouteService.insertStopIntoRoute(dto);
            return ResponseEntity.ok(Map.of("success", true, "message", "정류장 삽입 완료"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @Operation(summary = "노선링크 정류소 삭제", description = "노선링크의 정류소를 삭제해줌 *기존의 노선의 정류소도 삭제 가능하니 조심!")
    @DeleteMapping("/delete-stop")
    public ResponseEntity<?> deleteStop(@RequestParam String routeId,
                                        @RequestParam String moveDir,
                                        @RequestParam int seq) {
        try {
            deleteStopFromRouteService.deleteStopFromRoute(routeId, moveDir, seq);
            return ResponseEntity.ok("정류소 삭제 완료");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "노선 삭제", description = "노선을 삭제해줌 *기존의 노선도 삭제 가능하니 조심!")
    @DeleteMapping("/deleteRoute")
    public ResponseEntity<?> deleteRoute(@RequestParam String routeId) {
        try {
            deleteRouteService.deleteRoute(routeId);
            return ResponseEntity.ok(Map.of("success", true, "message", "노선 삭제 완료"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "삭제 실패: " + e.getMessage()));
        }
    }


    @GetMapping("/findRoutes")
    public ResponseEntity<List<RouteResultDTO>> findRoutes(
            @RequestParam String startBsId,
            @RequestParam String endBsId) {

        List<RouteResultDTO> directResults = routeFinderService.findRoutesWithNearbyStart(startBsId, endBsId);
        List<RouteResultDTO> transferResults = routeFinderService.findRoutesWithNearbyStart2(startBsId, endBsId);

        List<RouteResultDTO> combinedResults = new ArrayList<>();
        combinedResults.addAll(directResults);
        combinedResults.addAll(transferResults);

        return ResponseEntity.ok(combinedResults);
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
