package kroryi.bus2.controller;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import kroryi.bus2.dto.BusStopDTO;
import kroryi.bus2.entity.BusStop;
import kroryi.bus2.service.BusInfoInitService;
import kroryi.bus2.service.BusRedisService;
import kroryi.bus2.service.BusStopDataService;
import kroryi.bus2.service.RouteDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private final BusRedisService busRedisService;

    @Value("${api.service-key-decoding}")
    private String serviceKey;


    // 전체 버스정류장 불러오는거, 데이터가 너무 많아서 5개만 불러옴 이젠 안씀 추후 삭제 예정
    @GetMapping(value = "/busStops", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<BusStopDTO>> getBusStop() throws JsonProcessingException {

        List<BusStopDTO> list = busStopDataService.getAllBusStops();
        log.info("데이터 : {}", objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(list));
        return ResponseEntity.ok(list);
    }


    // 이건 웹에서 정류장 클릭하면 해당 정류장의 버스 도착 정보 날려주는거
    // @param bsId 정류장 ID
    // @return 해당 정류장의 도착 예정 버스 정보 (JSON 형식)
    @GetMapping("/bus-arrival")
    public ResponseEntity<JsonNode> getBusArrival(@RequestParam String bsId) throws JsonProcessingException {
        String jsonString = busStopDataService.getRedisBusStop(bsId);
        ObjectMapper mapper = new ObjectMapper();

        return ResponseEntity.ok(mapper.readTree(jsonString));
    }

    // 사용자가 검색창에 키워드를 입력했을 때, 해당 키워드에 해당하는 정류장명 또는 버스 노선명을 검색하여 반환
    // @param request { "keyword": "검색어" }
    // @return 정류장 목록과 버스 노선 번호 리스트를 포함한 JSON 응답
    @GetMapping(value = "/searchBSorBN", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> searchBSOrBN(@RequestParam String keyword) throws JsonProcessingException {

        System.out.println("검색어 : " + keyword);

        List<BusStop> busStop = busStopDataService.getBusStopsByNm(keyword);
//        log.info("정류소 데이터 : {}", objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(busStop));

        System.out.println("-----------------------------------");

        List<String> busNumber = routeDataService.getBusByNm(keyword);
//        log.info("버스 노선 데이터 : {}", objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(busNumber));

        Map<String, Object> response = new HashMap<>();
        response.put("busStops", busStop);
        response.put("busNumbers", busNumber);

        return ResponseEntity.ok(response);
    }

    @GetMapping(value = "/bus-route", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<JsonNode>> getBusRoute(@RequestParam String routeNo) throws IOException {
        List<JsonNode> result = routeDataService.getBusRoute(routeNo);

        return ResponseEntity.ok(result);
    }




//     이건 웹에서 정류장 클릭하면 해당 정류장의 버스 도착 정보 날려주는거
//    @GetMapping("/nav")
//    public ResponseEntity<JsonNode> getBusNav(@RequestParam String bsId) {
//        System.out.println("받은 bsId: " + bsId);
//
//        String API_URL = "https://apis.data.go.kr/6270000/dbmsapi01/getRealtime?";
//        String apiUrl = API_URL + "serviceKey=" + URLEncoder.encode(serviceKey, StandardCharsets.UTF_8) + "&bsId=" + bsId;
//
//        JsonNode jsonNode = busDataService.getBusStopNav(apiUrl);
//        return ResponseEntity.ok(jsonNode);
//    }








    // 얘는 db에 기초종합정보 넣는거 이젠 쓰지마시길 렉 걸림 (나중에 하루에 한번 자동으로 실행되어 데이터 갱싱용으로 바꿀 예정)
    @PostMapping("/fetch")
    public String fetchPostBusData() {
//        String routeId = "1000001000";
        String API_URL = "https://apis.data.go.kr/6270000/dbmsapi01/getBasic?";
        String apiUrl = API_URL + "serviceKey=" + URLEncoder.encode(serviceKey, StandardCharsets.UTF_8);

        busInfoInitService.fetchAndSaveBusData(apiUrl);
        return "Bus data fetched and saved successfully!";
    }
}
