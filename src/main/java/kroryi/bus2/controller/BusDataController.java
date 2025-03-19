package kroryi.bus2.controller;


import kroryi.bus2.service.BusDataService;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/bus")
public class BusDataController {
    private final BusDataService busDataService;

    public BusDataController(BusDataService busDataService) {
        this.busDataService = busDataService;
    }

//    @GetMapping("/fetch")
//    public String fetchGetBusData() {
//        String routeId = "1000001000";
//        String API_URL = "https://apis.data.go.kr/6270000/dbmsapi01/getBasic?";
//        String serviceKey = "";
//        String apiUrl = API_URL + "serviceKey=" + URLEncoder.encode(serviceKey, StandardCharsets.UTF_8);
//
//        busDataService.fetchAndSaveBusData(apiUrl);
////        return ResponseEntity.ok(busDataService.fetchAndSaveBusData(apiUrl));
//        return "입력 성공";
//    }

    @PostMapping("/fetch")
    public String fetchPostBusData() {
//        String routeId = "1000001000";
        String API_URL = "https://apis.data.go.kr/6270000/dbmsapi01/getBasic?";
        String serviceKey = "";   // 본인의 인증키(디코딩)
        String apiUrl = API_URL + "serviceKey=" + URLEncoder.encode(serviceKey, StandardCharsets.UTF_8);

        busDataService.fetchAndSaveBusData(apiUrl);
        return "Bus data fetched and saved successfully!";
    }
}
