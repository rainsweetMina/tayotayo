package kroryi.bus2.controller.board;

import kroryi.bus2.entity.busStop.BusStopInfo;
import kroryi.bus2.service.board.BusStopInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class BusStopInfoController {
    private final BusStopInfoService busStopInfoService;

    @GetMapping("/bus-info")
    public String showBusStopInfo(Model model) {
        model.addAttribute("district", busStopInfoService.getAllDistricts());
        return "board/busStopInfo";
        }
}
