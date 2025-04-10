package kroryi.bus2.controller.board;

import io.swagger.v3.oas.annotations.Operation;
import kroryi.bus2.dto.BusScheduleDto;
import kroryi.bus2.dto.SchedulePayloadDTO;
import kroryi.bus2.dto.busStop.BusStopDTO;
import kroryi.bus2.entity.BusSchedule;
import kroryi.bus2.repository.jpa.board.RouteStopLinkRepository;
import kroryi.bus2.repository.jpa.route.RouteRepository;
import kroryi.bus2.repository.jpa.board.BusScheduleRepository;
import kroryi.bus2.service.board.BusScheduleService;
import kroryi.bus2.service.board.RouteStopLinkService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class BusScheduleController {

    private final BusScheduleRepository busScheduleRepository;
    private final RouteStopLinkRepository routeStopLinkRepository;
    private final RouteRepository routeRepository;
    private final RouteStopLinkService routeStopLinkService;
    private final BusScheduleService busScheduleService;

    // 스케줄 데이터 조회
    @GetMapping("/schedule")
    public String showSchedule(Model model) {
        List<String> routeNos = busScheduleRepository.findDistinctRouteNos();
        model.addAttribute("routeNos", routeNos);
        return "/board/busSchedule";
    }

    // 수정 페이지 스케줄 데이터 조회
    @GetMapping("/modify-schedule")
    public String showModifySchedule(Model model) {
        List<String> routeNos = routeRepository.findDistinctRouteNos();
        model.addAttribute("routeNos", routeNos);
        return "/board/busModifySchedule";
    }

    // 수정 페이지에서 스케줄 테이블 수정
    @PostMapping("/api/modify-schedule")
    public ResponseEntity<String> updateSchedules(@RequestBody SchedulePayloadDTO request) {
        try {
            if (request.getSchedules() != null && !request.getSchedules().isEmpty()) {
                busScheduleService.saveOrUpdateSchedules(request.getSchedules());
            }

            if (request.getDeletedIds() != null && !request.getDeletedIds().isEmpty()) {
                busScheduleService.deleteSchedulesByIds(request.getDeletedIds());
            }

            return ResponseEntity.ok("success");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("스케줄 처리 중 오류 발생: " + e.getMessage());
        }
    }

    // 해당 노선의 방면 정보 가져오기
    @Operation(summary = "버스 노선 방면 조회", description = "버스 번호로 운행하는 방면 조회")
    @GetMapping("/api/route-notes")
    @ResponseBody
    public List<String> getRouteNotesByRouteNo(@RequestParam String routeNo,
                                               @RequestHeader(value = "Referer", required = false) String referer
    ) {
        if (referer != null && referer.contains("/modify-schedule")) {
            // 수정 페이지에서 요청한 경우
            return routeRepository.findDistinctRouteNoteByRouteNo(routeNo);
        } else {
            // 일반 페이지에서 요청한 경우
            return busScheduleRepository.findDistinctRouteNoteByRouteNo(routeNo);
        }
    }

    // 해당 노선 번호의 방향 목록 조회
    @Operation(summary = "버스 노선 방향 조회", description = "해당 노선 방향 조회(moveDIr 정방향:0, 역방향:1)")
    @GetMapping("/api/route-id/movedirs")
    @ResponseBody
    public List<String> getRouteDirections(@RequestParam(required = false) String routeId,
                                           @RequestParam(required = false) String routeNo,
                                           @RequestHeader(value = "Referer", required = false) String referer) {

        if (routeId != null && !routeId.isBlank()) {
            return routeStopLinkRepository.findDistinctMoveDirByRouteId(routeId); // routeId로 직접 조회 시
        }

        if (routeNo != null && !routeNo.isBlank()) {
            // 수정 페이지면 전체 방향 보여줌
            if (referer != null && referer.contains("/modify-schedule")) {
                return routeStopLinkRepository.findDistinctMoveDirsByRouteNo(routeNo);
            } else {
                // 일반 페이지에서는 실제 스케줄 있는 방향만 보여줌
                return busScheduleRepository.findDistinctMoveDirsByRouteNo(routeNo);
            }
        }

        return List.of();
    }

    // 노선 전체 정거장 조회
    @Operation(summary = "노선 정거장 조회", description = "방면있는 노선은 노선ID로만 조회가능 아닌경우 moveDir(정방향:0, 역방향:1) ")
    @GetMapping("/api/route-map")
    @ResponseBody
    public List<BusStopDTO> getRouteMap(@RequestParam String routeId, @RequestParam(required = false) String moveDir) {
        if (moveDir != null) {
            return routeStopLinkService.getStopsWithNamesByRouteIdAndMoveDir(routeId, moveDir);
        }
        return routeStopLinkService.getStopsWithNamesByRouteId(routeId);
    }

    // 해당 데이터 테이블 가져오기
    @Operation(summary = "해당 노선의 모든 시간표 조회", description = "routeNo & (routeNote or moveDir)로 해당 노선 스케줄 전체 조회")
    @GetMapping("/api/schedules")
    @ResponseBody
    public List<BusSchedule> getSchedulesByRouteInfo(@RequestParam String routeNo,
                                                     @RequestParam(required = false) String routeNote,
                                                     @RequestParam(required = false) String moveDir) {

        String routeId = null;

        if (routeNote != null && !routeNote.isBlank()) {
            routeId = routeRepository.findRouteIdByRouteNoAndNote(routeNo, routeNote);
        } else if (moveDir != null && !moveDir.isBlank()) {
            routeId = routeStopLinkRepository.findRouteIdByRouteNoAndMoveDir(routeNo, moveDir);
        }

        if (routeId == null) {
            return List.of();
        }

        // moveDir도 같이 넘겨서 정확한 스케줄만 조회
        if (moveDir != null && !moveDir.isBlank()) {
            return busScheduleRepository.findByRouteIdAndMoveDir(routeId, moveDir);
        }

        return busScheduleRepository.findByRouteId(routeId);
    }


    // 노선,방면,방향으로 노선ID 조회
    @Operation(summary = "해당 노선의 노선ID 조회(방면)", description = "버스번호와 방면으로 routeId 조회")
    @GetMapping("/api/route-id")
    @ResponseBody
    public String getRouteIdByRouteNoAndNote(@RequestParam String routeNo,
                                             @RequestParam(required = false) String routeNote,
                                             @RequestParam(required = false) String moveDir) {
        if (routeNote != null && !routeNote.isBlank()) {
            return routeRepository.findRouteIdByRouteNoAndNote(routeNo, routeNote);
        } else if (moveDir != null) {
            return routeStopLinkRepository.findRouteIdByRouteNoAndMoveDir(routeNo, moveDir);
        }
        return null;
    }

    @Operation(summary = "해당 노선의 노선ID 조회(방향)", description = "버스번호와 방향으로 routeId 조회")
    @GetMapping("/api/route-id/by-movedir")
    @ResponseBody
    public String getRouteIdByMoveDir(@RequestParam String routeNo, @RequestParam String moveDir) {
        List<String> routeIds = routeStopLinkRepository.findDistinctRouteIdByRouteNoAndMoveDir(routeNo, moveDir);
        return routeIds.isEmpty() ? "" : routeIds.get(0); // 첫 번째 routeId 반환
    }

    // 관리자용 스케줄 추가
    @Operation(summary = "스케줄 추가", description = "moveDir(정방향 : 0, 역방향 : 1) / busTCd(일반 : N, 저상 : D)")
    @PostMapping("/api/schedule/add")
    @ResponseBody
    public ResponseEntity<?>  createSchedule(@RequestBody BusScheduleDto dto) {
        BusSchedule saved = busScheduleService.saveSchedule(dto);
        return ResponseEntity.ok(saved);
    }

    // 관리자용 스케줄 수정
    @PutMapping("/api/schedule/modify")
    @ResponseBody
    @Operation(summary = "스케줄 수정", description = "moveDir(정방향 : 0, 역방향 : 1) / busTCd(일반 : N, 저상 : D)")
    public ResponseEntity<?> update(@RequestBody BusScheduleDto dto,
                                    @RequestParam Long id) {
        if (!busScheduleRepository.existsById(id)) {
            return ResponseEntity.badRequest().body("존재하지 않는 스케줄입니다: " + id);
        }

        BusSchedule updated = busScheduleService.updateSchedule(id, dto);
        return ResponseEntity.ok(updated);
    }

    // 관리자용 스케줄 삭제
    @DeleteMapping("/api/schedule/delete")
    @ResponseBody
    @Operation(summary = "스케줄 삭제", description = "스케줄번호를 적지않을 경우 해당 노선의 시간표가 전부 삭제될 수 있으니 주의바람")
    public ResponseEntity<?> deleteSchedule(@RequestParam String routeId,
                                            @RequestParam(required = false) String moveDir,
                                            @RequestParam(required = false) Integer scheduleNo) {
        try {
            busScheduleService.deleteSchedules(routeId, moveDir, scheduleNo);
            return ResponseEntity.ok("삭제 완료");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("삭제 중 오류 발생: " + e.getMessage());
        }
    }

}