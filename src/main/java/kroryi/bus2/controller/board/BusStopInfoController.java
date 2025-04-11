package kroryi.bus2.controller.board;

import kroryi.bus2.entity.busStop.BusStopInfo;
import kroryi.bus2.repository.jpa.board.BusStopInfoRepository;
import kroryi.bus2.service.board.BusStopInfoService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class BusStopInfoController {
    private final BusStopInfoRepository busStopInfoRepository;
    private final BusStopInfoService busStopInfoService;

    @GetMapping("/bus-info")
    public String showBusStopInfo(Model model) {
        List<BusStopInfo> busStopInfos = busStopInfoRepository.findAll();
        model.addAttribute("busStopInfos", busStopInfos);
        return "/board/busStopInfo";
    }
}
