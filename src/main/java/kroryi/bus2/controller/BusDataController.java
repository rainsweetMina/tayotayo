package kroryi.bus2.controller;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kroryi.bus2.dto.BusStopDTO;
import kroryi.bus2.entity.BusStop;
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


    @GetMapping(value = "/busStops", produces = MediaType.APPLICATION_JSON_VALUE)  // JSON 응답 강제
    public ResponseEntity<List<BusStopDTO>> getBusStop() throws JsonProcessingException {

        List<BusStopDTO> list = busStopDataService.getAllBusStops();
        log.info("데이터 : {}", objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(list));
        return ResponseEntity.ok(list);
    }

    @PostMapping("/fetch")
    public String fetchPostBusData() {
//        String routeId = "1000001000";
        String API_URL = "https://apis.data.go.kr/6270000/dbmsapi01/getBasic?";
        String apiUrl = API_URL + "serviceKey=" + URLEncoder.encode(serviceKey, StandardCharsets.UTF_8);

        busDataService.fetchAndSaveBusData(apiUrl);
        return "Bus data fetched and saved successfully!";
    }
}
