package kroryi.bus2.controller;

import kroryi.bus2.service.BusDataService;
import kroryi.bus2.service.BusRedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.function.client.WebClient;

@Controller
@RequestMapping("/")
@RequiredArgsConstructor
public class BusController {

    private final BusDataService busDataService;
    private final BusRedisService busRedisService;

    @GetMapping("/bus")
    public String getBusStops() {
        return "bus/busStops";
    }

    @GetMapping("/bus2")
    public String getBusStops2() {
        return "bus/busStops2";
    }


    @GetMapping("/arrival")
    public String getBusArrival(@RequestParam String busStopId) {
        return busRedisService.getBusArrival(busStopId);
    }

    @GetMapping
    public String loadBusStopsToRedis(){
        busRedisService.loadBusStopsToRedis();
        return "Redis에 버스 정류장 데이터 저장 완료";
    }

}
