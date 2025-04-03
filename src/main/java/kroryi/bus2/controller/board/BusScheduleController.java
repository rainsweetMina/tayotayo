package kroryi.bus2.controller.board;

import kroryi.bus2.entity.board.BusSchedule;
import kroryi.bus2.repository.jpa.RouteRepository;
import kroryi.bus2.repository.board.BusScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class BusScheduleController {

    private final BusScheduleRepository scheduleRepository;
    private final RouteRepository routeRepository;

    @GetMapping("/schedule")
    public String showSchedule(Model model) {
        List<String> routeNos = routeRepository.findDistinctRouteNos();
        model.addAttribute("routeNos", routeNos);

        List<String> routeNotes = routeRepository.findDistinctRouteNotes() ;
        model.addAttribute("routeNotes",  routeNotes);

        return "/board/busSchedule";
    }

    @PostMapping("/api/modify-schedule")
    public ResponseEntity<String> updateSchedules(@RequestBody List<BusSchedule> updatedSchedules) {
        for (BusSchedule schedule : updatedSchedules) {
            BusSchedule original = scheduleRepository.findById((long) schedule.getScheduleNo())
                    .orElseThrow(() -> new RuntimeException("스케줄 없음"));

            original.setSchedule_A(schedule.getSchedule_A());
            original.setSchedule_B(schedule.getSchedule_B());
            original.setSchedule_C(schedule.getSchedule_C());
            original.setSchedule_D(schedule.getSchedule_D());
            original.setSchedule_E(schedule.getSchedule_E());
            original.setSchedule_F(schedule.getSchedule_F());
            original.setSchedule_G(schedule.getSchedule_G());
            original.setSchedule_H(schedule.getSchedule_H());

            scheduleRepository.save(original);
        }
        return ResponseEntity.ok("success");
    }

    @GetMapping("/api/route-notes")
    @ResponseBody
    public List<String> getRouteNotesByRouteNo(@RequestParam String routeNo) {
        return routeRepository.findDistinctRouteNoteByRouteNo(routeNo);
    }

    // 해당 데이터 테이블 가져오기
    @GetMapping("/api/schedules")
    @ResponseBody
    public List<BusSchedule> getSchedulesByRouteInfo(@RequestParam String routeNo,
                                                     @RequestParam String routeNote) {

        String routeId = routeRepository.findRouteIdByRouteNoAndNote(routeNo, routeNote);
        if (routeId == null) {
            return List.of(); // 데이터 없으면 빈 리스트 반환
        }

        return scheduleRepository.findByRouteId(routeId);
    }


}