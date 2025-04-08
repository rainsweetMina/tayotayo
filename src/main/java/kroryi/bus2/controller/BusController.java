package kroryi.bus2.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
@RequiredArgsConstructor
//  버스,노선 관련 페이지 요청을 처리하는 컨트롤러입니다.
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

    // 버스의 노선 추가 하는 페이지
    @GetMapping("/AddRouteStopLink")
    public String AddBusRoute() {
        return "bus/addRouteStopLink";
    }

    // 버스 노선 불러오는 페이지
    @GetMapping("/GetRouteStopLink")
    public String getBusRoute() {
        return "bus/getRouteStopLink";
    }

    // 커스텀 노선 정보 불러오는 페이지
    @GetMapping("/UpdateCustomRouteInfo")
    public String UpdateCustomRoute() {
        return "bus/updateCustomRouteInfo";
    }

    // 커스텀 노선의 순서 변경하는 페이지
    @GetMapping("/UpdateRouteLink")
    public String UpdateRouteLink() {
        return "bus/updateRouteLinkSeq";
    }

    // 노선에 정류장 추가하는 페이지
    @GetMapping("/InsertStop")
    public String InsertStop() {
        return "bus/InsertStopIntoRouteLink";
    }


//    @GetMapping
//    public String loadBusStopsToRedis(){
//        busRedisService.loadBusStopsToRedis();
//        return "Redis에 버스 정류장 데이터 저장 완료";
//    }




}
