package kroryi.bus2.controller;

import io.swagger.v3.oas.annotations.Operation;
import kroryi.bus2.dto.Route.CustomRouteRegisterRequestDTO;
import kroryi.bus2.dto.RouteStopLinkDTO;
import kroryi.bus2.service.CustomeRoute.AddCustomRouteService;
import kroryi.bus2.service.CustomeRoute.AddRouteStopLinkService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/add")
@RequiredArgsConstructor
@Log4j2
public class AddRouteController {

    private final AddCustomRouteService addCustomRouteService;
    private final AddRouteStopLinkService addRouteStopLinkService;

    // 경유 정류소만 추가 거의 쓸일없을듯?
    @PostMapping("/AddRouteStopLink")
    @Operation(summary = "Custom 경유지 추가", description = "새로운 Custom 경유 정류소만 추가 거의 쓸일없을듯?")
    public void addRouteStopLink(@RequestBody List<RouteStopLinkDTO> dtoList) {
        System.out.println("받아온 데이터 : " + dtoList);
        addRouteStopLinkService.saveAll(dtoList);
    }

    // 노선만들기 + 경유 정류소 추가
    @PostMapping("/AddBusRoute")
    @Operation(summary = "Custom 노선 + 경유지 추가", description = "새로운 Custom 노선 + 경유지를 추가합니다.")
    public ResponseEntity<?> addRoute(@RequestBody CustomRouteRegisterRequestDTO request) {
        try {
            addCustomRouteService.saveFullRoute(request);
            return ResponseEntity.ok(Map.of("success", true, "routeId", request.getRoute().getRouteId()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }



}
