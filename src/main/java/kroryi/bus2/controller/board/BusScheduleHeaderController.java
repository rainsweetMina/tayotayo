package kroryi.bus2.controller.board;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.Operation;
import kroryi.bus2.service.board.BusScheduleHeaderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class BusScheduleHeaderController {

    private final BusScheduleHeaderService busScheduleHeaderService;

    // 노선별 지정 정거장 저장
    @Operation(summary = "노선별 지정 정거장 저장", description = "시간표에 필요한 정거장 목록 저장(시작, 끝 종점을 제외한 정거장) ")
    @PostMapping("/schedule-header")
    public ResponseEntity<Void> saveHeader(@RequestBody Map<String, Object> body) throws JsonProcessingException {
        String routeId = (String) body.get("routeId");
        String moveDir = (String) body.get("moveDir");
        List<Integer> stopOrder = (List<Integer>) body.get("stopOrder");

        busScheduleHeaderService.saveStopOrder(routeId, moveDir, stopOrder);
        return ResponseEntity.ok().build();
    }

    // 시간표에 헤드(지정 정거장) 조회
    @Operation(summary = "노선별 선택 정류장 조회", description = "시간표에 헤드(버스 정거장) 목록 조회")
    @GetMapping("/schedule-header")
    public ResponseEntity<List<Integer>> getHeader(
            @RequestParam String routeId,
            @RequestParam(required = false) String moveDir) {
        return ResponseEntity.ok(busScheduleHeaderService.getStopOrder(routeId, moveDir));
    }
}
