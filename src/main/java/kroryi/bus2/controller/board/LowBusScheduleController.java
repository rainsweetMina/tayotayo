package kroryi.bus2.controller.board;

import kroryi.bus2.entity.BusSchedule;
import kroryi.bus2.repository.jpa.board.BusScheduleRepository;
import kroryi.bus2.repository.jpa.route.RouteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class LowBusScheduleController {
    private final RouteRepository routeRepository;
    private final BusScheduleRepository busScheduleRepository;

    @GetMapping("/low-schedule")
    public String showLowFloorSchedule(Model model) {
        List<String> routeNos = routeRepository.findDistinctRouteNos();
        model.addAttribute("routeNos", routeNos);
        return "/board/lowBusSchedule";
    }

    @GetMapping("/api/lowbus-scheduls")
    @ResponseBody
    public List<BusSchedule> getLowBusSchedules(@RequestParam String routeId,
                                                @RequestParam(required = false) String moveDir) {
        if (moveDir != null && !moveDir.isBlank()) {
            return busScheduleRepository.findByRouteIdAndMoveDirAndBusTCd(routeId, moveDir, "D");
        }
        return busScheduleRepository.findByRouteIdAndBusTCd(routeId, "D");
    }

}
