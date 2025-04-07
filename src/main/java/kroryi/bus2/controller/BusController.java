package kroryi.bus2.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
@RequiredArgsConstructor
//  버스 정류장 관련 페이지 요청을 처리하는 컨트롤러입니다.
public class BusController {

    // "/bus" 요청을 처리하여 busStops 페이지로 이동
    @GetMapping("/bus")
    public String getBusStops() {
        return "bus/busStops";
    }

    // 이건 테스트용인듯
    @GetMapping("/bus2")
    public String getBusStops2() {
        return "bus/busStops2";
    }



    // 버스의 노선 추가 하는거
    @GetMapping("/AddRouteStopLink")
    public String AddBusRoute() {
        return "bus/addRouteStopLink";
    }

    // 버스 노선 불러오는거
    @GetMapping("/GetRouteStopLink")
    public String getBusRoute() {
        return "bus/getRouteStopLink";
    }


//    @GetMapping
//    public String loadBusStopsToRedis(){
//        busRedisService.loadBusStopsToRedis();
//        return "Redis에 버스 정류장 데이터 저장 완료";
//    }




}
