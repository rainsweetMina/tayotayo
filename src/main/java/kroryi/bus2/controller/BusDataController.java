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
//        String serviceKey = "j/gLHENNg0EDmUOP1OcG5WafUwAUq0u6D1CAZp7xdSTLsSmRJ7r6Pfi34Ks2ZZ7lM0zVZHjjESDToVIX+soPGA==";
//        String apiUrl = API_URL + "serviceKey=" + URLEncoder.encode(serviceKey, StandardCharsets.UTF_8);
//
//        busDataService.fetchAndSaveBusData(apiUrl);
////        return ResponseEntity.ok(busDataService.fetchAndSaveBusData(apiUrl));
//        return "입력 성공";
//    }

    @PostMapping("/fetch")
    public String fetchPostBusData() {
        String routeId = "1000001000";
        String API_URL = "https://apis.data.go.kr/6270000/dbmsapi01/getBasic?";
        String serviceKey = "j/gLHENNg0EDmUOP1OcG5WafUwAUq0u6D1CAZp7xdSTLsSmRJ7r6Pfi34Ks2ZZ7lM0zVZHjjESDToVIX+soPGA==";
        String apiUrl = API_URL + "serviceKey=" + URLEncoder.encode(serviceKey, StandardCharsets.UTF_8);

        busDataService.fetchAndSaveBusData(apiUrl);
        return "Bus data fetched and saved successfully!";
    }
}
