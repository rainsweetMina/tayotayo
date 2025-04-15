package kroryi.bus2.controller.board;

import io.swagger.v3.oas.annotations.Operation;
import kroryi.bus2.entity.busStop.BusStopInfo;
import kroryi.bus2.service.board.BusStopInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/bus-info")
@RequiredArgsConstructor
public class BusStopInfoApiController {
    private final BusStopInfoService busStopInfoService;

    @Operation(summary = "구 목록 조회")
    @GetMapping("/districts")
    public List<String> getDistricts() {
        return busStopInfoService.getAllDistricts();
    }

    @Operation(summary = "동 목록 조회", description = "군, 구로 검색")
    @GetMapping("/neighborhoods")
    public List<String> getNeighborhoods(@RequestParam String district) {
        return busStopInfoService.getNeighborhoodsByDistrict(district);
    }

    @Operation(summary = "정류소 정보 조회")
    @GetMapping("/search")
    public List<BusStopInfo> search(
            @RequestParam String district,
            @RequestParam(required = false) String neighborhood) {
        if (neighborhood != null && !neighborhood.isBlank()) {
            return busStopInfoService.getBusStopsByDistrictAndNeighborhood(district, neighborhood);
        } else {
            return busStopInfoService.getBusStopsByDistrict(district);
        }
    }

    @Operation(summary = "노선 유형에 해당하는 노선 번호 목록 반환", description = "Type : 급행, 순환, 간선, 지선")
    @GetMapping("/route-nos")
    public List<String> getRouteNosByType(@RequestParam String type) {
        return busStopInfoService.getRouteNosByType(type);
    }

    @Operation(summary = "노선 번호로 정류소 목록 조회")
    @GetMapping("/search-by-route")
    public List<BusStopInfo> getStopsByRouteNo(@RequestParam String routeNo) {
        return busStopInfoService.getStopsByRouteNo(routeNo);
    }


}
