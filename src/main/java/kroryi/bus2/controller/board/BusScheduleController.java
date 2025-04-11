package kroryi.bus2.controller.board;

import kroryi.bus2.repository.jpa.route.RouteRepository;
import kroryi.bus2.repository.jpa.board.BusScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class BusScheduleController {

    private final BusScheduleRepository busScheduleRepository;
    private final RouteRepository routeRepository;

    // 스케줄 데이터 조회
    @GetMapping("/schedule")
    public String showSchedule(Model model) {
        List<String> routeNos = busScheduleRepository.findDistinctRouteNos();
        model.addAttribute("routeNos", routeNos);
        return "/board/busSchedule";
    }

    // 수정 페이지 스케줄 데이터 조회
    @GetMapping("/admin/schedule")
    public String showModifySchedule(Model model) {
        List<String> routeNos = routeRepository.findDistinctRouteNos();
        model.addAttribute("routeNos", routeNos);
        return "/board/busModifySchedule";
    }

}