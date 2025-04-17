package kroryi.bus2.controller.board;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import kroryi.bus2.dto.board.BusScheduleHeaderRequestDTO;
import kroryi.bus2.entity.BusScheduleHeader;
import kroryi.bus2.service.board.BusScheduleHeaderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class BusScheduleHeaderApiController {

    private final BusScheduleHeaderService busScheduleHeaderService;

    // 시간표에 헤드(지정 정거장) 조회
    @Operation(summary = "노선별 선택 정류장 조회", description = "시간표에 헤드(버스 정거장) 목록 조회")
    @GetMapping("/schedule-header")
    public ResponseEntity<List<Integer>> getHeader(
            @RequestParam String routeId,
            @RequestParam(required = false) String moveDir) {
        return ResponseEntity.ok(busScheduleHeaderService.getStopOrder(routeId, moveDir));
    }

    // 노선별 지정 정거장 저장
    @Hidden
    @Operation(summary = "노선도 표시용 정류장 저장 (웹)", description = "시간표에 필요한 정거장 목록 저장(시작, 끝 종점을 제외한 정거장) ")
    @PostMapping("/schedule-header")
    public ResponseEntity<Void> saveHeader(@RequestBody Map<String, Object> body) throws JsonProcessingException {
        String routeId = (String) body.get("routeId");
        String moveDir = (String) body.get("moveDir");
        List<Integer> stopOrder = (List<Integer>) body.get("stopOrder");

        busScheduleHeaderService.saveStopOrder(routeId, moveDir, stopOrder);
        return ResponseEntity.ok().build();
    }

    // 관리자용 추가
    @Operation(summary = "노선별 선택 정류장 추가", description = "노선 선택 버스 정거장 목록 추가")
    @PostMapping("/schedule-headers")
    public ResponseEntity<BusScheduleHeader> create(@RequestBody BusScheduleHeaderRequestDTO dto) {
        return ResponseEntity.ok(busScheduleHeaderService.create(dto));
    }

    // 관리자용 조회
    @Operation(summary = "노선별 선택 정류장 전체 조회", description = "노선 선택 버스 정거장 목록 조회")
    @GetMapping("/schedule-headers")
    public ResponseEntity<List<BusScheduleHeader>> findAll() {
        return ResponseEntity.ok(busScheduleHeaderService.findAll());
    }

    // 관리자용 수정
    @Operation(summary = "노선별 선택 정류장 수정", description = "노선 선택 버스 정거장 목록 수정")
    @PutMapping("/schedule-headers/{id}")
    public ResponseEntity<BusScheduleHeader> update(@PathVariable int id, @RequestBody BusScheduleHeaderRequestDTO dto) {
        return ResponseEntity.ok(busScheduleHeaderService.update(id, dto));
    }

    // 관리자용 삭제
    @Operation(summary = "노선별 선택 정류장 삭제", description = "노선 선택 버스 정거장 목록 조회")
    @DeleteMapping("/schedule-headers/{id}")
    public ResponseEntity<Void> delete(@PathVariable int id) {
        busScheduleHeaderService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "해당 노선 정거장 조회", description = "해당 노선 정거장 전체 조회")
    @GetMapping("/route-stops/sequences")
    public ResponseEntity<?> getRouteSeqs(@RequestParam String routeId,
                                          @RequestParam(required = false) String moveDir) {
        if(moveDir == null) {
            return ResponseEntity.ok(busScheduleHeaderService.getSeqsByRoute(routeId));
        }else {
            return ResponseEntity.ok(busScheduleHeaderService.getSeqsByRouteIdAndMoveDir(routeId, moveDir));
        }

    }

    @Operation(summary = "해당 노선 특정 정류장 조회", description = "해당 노선 정거장 순서(seq) 조회")
    @GetMapping("/route-stops/seq-by-name")
    public ResponseEntity<List<String>> getSeqByStopName(
            @RequestParam String routeId,
            @RequestParam String bsNm) {
        return ResponseEntity.ok(busScheduleHeaderService.findSeqsByRouteIdAndBusStopName(routeId, bsNm));
    }

}
