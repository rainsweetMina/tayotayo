package kroryi.bus2.controller.board;

import kroryi.bus2.entity.board.BusSchedule;
import kroryi.bus2.repository.RouteRepository;
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

        List<BusSchedule> list = scheduleRepository.findAll();
        model.addAttribute("schedules", list);
        return "/board/busSchedule";
    }

    @PostMapping("/api/save-schedule-bulk")
    public ResponseEntity<String> updateSchedules(@RequestBody List<BusSchedule> updatedSchedules) {
        for (BusSchedule schedule : updatedSchedules) {
            BusSchedule original = scheduleRepository.findById(schedule.getSchedule_no())
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

//    @GetMapping("/schedule/filter")
//    public String filterSchedule(
//            @RequestParam String routeNo,
//            @RequestParam String routeNote,
//            Model model) {
//
//        // 1. routeId 찾기
//        String routeId = routeRepository.findRouteIdByRouteNoAndNote(routeNo, routeNote);
//
//        // 2. 해당 routeId에 맞는 스케줄 조회
//        List<BusSchedule> schedules = scheduleRepository.findByRouteId(routeId);
//
//        // 3. 다시 모델에 넣기
//        model.addAttribute("routeNos", routeRepository.findDistinctRouteNos());
//        model.addAttribute("routeNotes", routeRepository.findDistinctRouteNotes());
//        model.addAttribute("schedules", schedules);
//
//        return "board/busSchedule";
//    }
}