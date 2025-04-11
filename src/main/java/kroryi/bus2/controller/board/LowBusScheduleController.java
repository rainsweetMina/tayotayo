package kroryi.bus2.controller.board;

import io.swagger.v3.oas.annotations.Operation;
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
        List<String> routeNos = busScheduleRepository.getRouteNosWithLowBus("D");   // 스케줄 데이터가 있는 노선만 검색
        model.addAttribute("routeNos", routeNos);
        return "/board/lowBusSchedule";
    }

    // 저상버스 스케줄 조회
    @Operation(summary = "저상버스 시간표 조회", description = "해당 노선 저상버스 스케줄 조회")
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
