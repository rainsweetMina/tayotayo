package kroryi.bus2.controller;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import kroryi.bus2.dto.BusStopDTO;
import kroryi.bus2.service.BusDataService;
import kroryi.bus2.service.BusStopDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/bus")
@RequiredArgsConstructor
@Log4j2
public class BusDataController {
    private final BusDataService busDataService;
    private final BusStopDataService busStopDataService;
    private final ObjectMapper objectMapper;

    @Value("${api.service-key}")
    private String serviceKey;


    // 전체 버스정류장 불러오는거, 데이터가 너무 많아서 5개만 불러옴
    @GetMapping(value = "/busStops", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<BusStopDTO>> getBusStop() throws JsonProcessingException {

        List<BusStopDTO> list = busStopDataService.getAllBusStops();
        log.info("데이터 : {}", objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(list));
        return ResponseEntity.ok(list);
    }

    // 이건 웹에서 정류장 클릭하면 해당 정류장의 버스 도착 정보 날려주는거
    @GetMapping("/nav")
    public ResponseEntity<JsonNode> getBusNav(@RequestParam String bsId) {
        System.out.println("받은 bsId: " + bsId);

        String API_URL = "https://apis.data.go.kr/6270000/dbmsapi01/getRealtime?";
        String apiUrl = API_URL + "serviceKey=" + URLEncoder.encode(serviceKey, StandardCharsets.UTF_8) + "&bsId=" + bsId;

        JsonNode jsonNode = busDataService.getBusStopNav(apiUrl);
        return ResponseEntity.ok(jsonNode);
    }

    // 얘는 db에 기초종합정보 넣는거 이젠 쓰지마시길 렉 걸림 (나중에 하루에 한번 자동으로 실행되어 데이터 갱싱용으로 바꿀 예정)
    @PostMapping("/fetch")
    public String fetchPostBusData() {
//        String routeId = "1000001000";
        String API_URL = "https://apis.data.go.kr/6270000/dbmsapi01/getBasic?";
        String apiUrl = API_URL + "serviceKey=" + URLEncoder.encode(serviceKey, StandardCharsets.UTF_8);

        busDataService.fetchAndSaveBusData(apiUrl);
        return "Bus data fetched and saved successfully!";
    }
}
