package kroryi.bus2.controller.board;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import kroryi.bus2.service.board.RouteStopLinkService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Hidden
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/route-stop-link")
public class RouteStopLinkFetchController {

    private final RouteStopLinkService routeStopLinkService;

    // 특정 노선
    @PostMapping("/fetch-one")
    @Operation(summary = "특정 노선 정거장 가져오기")
    public ResponseEntity<String> fetchSingleRoute(@RequestParam String routeId) {
        routeStopLinkService.fetchSingleRouteStopLink(routeId);
        return ResponseEntity.ok("✅ 단일 노선 저장 완료: " + routeId);
    }

    // 전체 노선
    @PostMapping("/fetch-all")
    @Operation(summary = "전체 노선 정거장 가져오기")
    public String fetchAllRoutes() {
        routeStopLinkService.fetchAndSaveAllRouteStopLinks();
        return "Bus data fetched and saved successfully!";
    }
}
