package kroryi.bus2.controller.board;

import kroryi.bus2.dto.SchedulePayload;
import kroryi.bus2.dto.busStop.BusStopDTO;
import kroryi.bus2.entity.BusSchedule;
import kroryi.bus2.repository.jpa.board.RouteStopLinkRepository;
import kroryi.bus2.repository.jpa.route.RouteRepository;
import kroryi.bus2.repository.jpa.board.BusScheduleRepository;
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

    private final BusScheduleRepository scheduleRepository;
    private final RouteStopLinkRepository routeStopLinkRepository;
    private final RouteRepository routeRepository;
    private final RouteStopLinkService routeStopLinkService;

    // 스케줄 데이터 조회
    @GetMapping("/schedule")
    public String showSchedule(Model model) {
        List<String> routeNos = routeRepository.findDistinctRouteNos();
        model.addAttribute("routeNos", routeNos);
        return "/board/busSchedule";
    }

    // 스케줄 테이블 수정
    @PostMapping("/api/modify-schedule")
    public ResponseEntity<String> updateSchedules(@RequestBody SchedulePayload request) {
        try {
            List<BusSchedule> updatedSchedules = request.getSchedules();
            List<Long> deletedIds = request.getDeletedIds();

            if (updatedSchedules != null && !updatedSchedules.isEmpty()) {

                for (BusSchedule schedule : updatedSchedules) {
                    if (schedule.getId() == null) {
                        // 신규 추가
                        BusSchedule newSchedule = new BusSchedule();
                        newSchedule.setRouteId(schedule.getRouteId());
                        newSchedule.setMoveDir(schedule.getMoveDir());
                        newSchedule.setBusTCd(schedule.getBusTCd() != null ? schedule.getBusTCd() : "");
                        newSchedule.setScheduleNo(schedule.getScheduleNo());
                        newSchedule.setSchedule_A(schedule.getSchedule_A());
                        newSchedule.setSchedule_B(schedule.getSchedule_B());
                        newSchedule.setSchedule_C(schedule.getSchedule_C());
                        newSchedule.setSchedule_D(schedule.getSchedule_D());
                        newSchedule.setSchedule_E(schedule.getSchedule_E());
                        newSchedule.setSchedule_F(schedule.getSchedule_F());
                        newSchedule.setSchedule_G(schedule.getSchedule_G());
                        newSchedule.setSchedule_H(schedule.getSchedule_H());

                        scheduleRepository.save(newSchedule);
                    } else {
                        // 수정
                        BusSchedule existing = scheduleRepository.findById(schedule.getId())
                                .orElseThrow(() -> new RuntimeException("해당 ID 없음"));

                        existing.setSchedule_A(schedule.getSchedule_A());
                        existing.setSchedule_B(schedule.getSchedule_B());
                        existing.setSchedule_C(schedule.getSchedule_C());
                        existing.setSchedule_D(schedule.getSchedule_D());
                        existing.setSchedule_E(schedule.getSchedule_E());
                        existing.setSchedule_F(schedule.getSchedule_F());
                        existing.setSchedule_G(schedule.getSchedule_G());
                        existing.setSchedule_H(schedule.getSchedule_H());

                        scheduleRepository.save(existing);
                    }
                }
            }
            if (deletedIds != null && !deletedIds.isEmpty()) {
                scheduleRepository.deleteAllByIdInBatch(deletedIds);
            }
            return ResponseEntity.ok("success");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("스케줄 처리 중 오류 발생: " + e.getMessage());
        }
    }

    // 해당 노선의 방면 정보 가져오기
    @GetMapping("/api/route-notes")
    @ResponseBody
    public List<String> getRouteNotesByRouteNo(@RequestParam String routeNo) {
        return routeRepository.findDistinctRouteNoteByRouteNo(routeNo);
    }

    // 해당 데이터 테이블 가져오기
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
            return scheduleRepository.findByRouteIdAndMoveDir(routeId, moveDir);
        }

        return scheduleRepository.findByRouteId(routeId);
    }

    @GetMapping("/api/route-map")
    @ResponseBody
    public List<BusStopDTO> getRouteMap(@RequestParam String routeId, @RequestParam(required = false) String moveDir) {
        if (moveDir != null) {
            return routeStopLinkService.getStopsWithNamesByRouteIdAndMoveDir(routeId, moveDir);
        }
        return routeStopLinkService.getStopsWithNamesByRouteId(routeId);
    }

    @GetMapping("/api/route-directions")
    @ResponseBody
    public List<String> getDirectionsByRouteNo(@RequestParam String routeNo) {
        String routeId = routeRepository.findRouteIdByRouteNoOnly(routeNo);
        if (routeId == null) return List.of();
        return routeStopLinkRepository.findDistinctMoveDirByRouteId(routeId);
    }


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

    @GetMapping("/api/route-id/movedirs")
    @ResponseBody
    public List<String> getMoveDirsByRouteNo(@RequestParam String routeNo) {
        return routeStopLinkRepository.findDistinctMoveDirsByRouteNo(routeNo);
    }

    @GetMapping("/api/route-id/by-movedir")
    @ResponseBody
    public String getRouteIdByMoveDir(@RequestParam String routeNo, @RequestParam String moveDir) {
        List<String> routeIds = routeStopLinkRepository.findDistinctRouteIdByRouteNoAndMoveDir(routeNo, moveDir);
        return routeIds.isEmpty() ? "" : routeIds.get(0); // 첫 번째 routeId 반환
    }


}