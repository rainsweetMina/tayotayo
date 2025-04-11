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

//    // 이건 테스트용인듯
//    @GetMapping("/bus2")
//    public String getBusStops2() {
//        return "bus/busStops2";
//    }

    // 전체 노선 불러오는 페이지
    @GetMapping("/GetAllRoute")
    public String GetAllBusRoute() {
        return "bus/route/getAllRoute";
    }

    // 전체 정류장 불러오는 페이지
    @GetMapping("/GetAllBusStop")
    public String GetAllBusStop() {
        return "bus/bus_stop/getAllBusStop";
    }


    // 버스의 노선 추가 하는 페이지
    @GetMapping("/AddRouteStopLink")
    public String AddBusRoute() {
        return "bus/route/addRouteStopLink";
    }

    // 버스 노선 불러오는 페이지
    @GetMapping("/GetRouteStopLink")
    public String getBusRoute() {
        return "bus/route/getRouteStopLink";
    }

    // 커스텀 노선 정보 불러오는 페이지
    @GetMapping("/UpdateRouteInfo")
    public String UpdateRoute() {
        return "bus/route/updateRouteInfo";
    }

    // 커스텀 노선의 순서 변경하는 페이지
    @GetMapping("/UpdateRouteLink")
    public String UpdateRouteLink() {
        return "bus/route/updateRouteLinkSeq";
    }

    // 노선에 정류장 추가하는 페이지
    @GetMapping("/InsertStop")
    public String InsertStop() {
        return "bus/route/InsertStopIntoRouteLink";
    }

    // 정류장을 새로 추가하는 페이지
    @GetMapping("/AddBusStop")
    public String addStop() {
        return "bus/bus_stop/addBusStop";
    }

    // 정류장을 상세정보 보는 페이지
    @GetMapping("/GetBusStop")
    public String getStop() {
        return "bus/bus_stop/getBusStop";
    }

    // 정류장의 정보를 수정하는 페이지
    @GetMapping("/UpdateBusStop")
    public String updateStop() {
        return "bus/bus_stop/updateBusStop";
    }

    // 정류장의 정보를 수정하는 페이지
    @GetMapping("/ApiTest")
    public String apitest() {
        return "/apiSampleTest";
    }


//    @GetMapping
//    public String loadBusStopsToRedis(){
//        busRedisService.loadBusStopsToRedis();
//        return "Redis에 버스 정류장 데이터 저장 완료";
//    }


    // 통합 메뉴
    @GetMapping("/TestMenu")
    public String TestMenu() {
        return "/TestMenu"; // templates/menu.html
    }

}
