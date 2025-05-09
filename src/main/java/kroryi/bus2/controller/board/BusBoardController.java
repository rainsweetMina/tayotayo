package kroryi.bus2.controller.board;

import kroryi.bus2.entity.BusCompany;
import kroryi.bus2.entity.BusFare;
import kroryi.bus2.repository.jpa.board.BusCompanyRepository;
import kroryi.bus2.repository.jpa.board.BusFareRepository;
import kroryi.bus2.repository.jpa.board.BusScheduleRepository;
import kroryi.bus2.repository.jpa.route.RouteRepository;
import kroryi.bus2.service.board.BusStopInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Collections;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class BusBoardController {
    private final BusStopInfoService busStopInfoService;
    private final BusFareRepository busFareRepository;
    private final BusCompanyRepository busCompanyRepository;
    private final BusScheduleRepository busScheduleRepository;
    private final RouteRepository routeRepository;

    // 정류장 조회 페이지
    @GetMapping("/bus-info")
    public String showBusStopInfo(Model model) {
        model.addAttribute("district", busStopInfoService.getAllDistricts());
        return "board/busStopInfo";
    }

    // 요금 정보 페이지
    @GetMapping("/fare")
    public String showBusFare(Model model) {
        List<BusFare> fareList = busFareRepository.findAll();
        model.addAttribute("fares", fareList);
        return "board/busFare";
    }

    // 버스 회사 정보 페이지
    @GetMapping("/bus-company")
    public String showBusCompany(Model model) {
        List<BusCompany> companyList = busCompanyRepository.findAll();
        model.addAttribute("companies", companyList);
        return "board/busCompany";
    }

    // 스케줄 데이터 조회
    @GetMapping("/schedule")
    public String showSchedule(Model model) {
        List<String> routeNos = busScheduleRepository.findDistinctRouteNos();
        Collections.sort(routeNos);
        model.addAttribute("routeNos", routeNos);
        return "board/busSchedule";
    }

    // 수정 페이지 스케줄 데이터 조회
    @GetMapping("/admin/schedule")
    public String showModifySchedule(Model model) {
        List<String> routeNos = routeRepository.findDistinctRouteNos();
        model.addAttribute("routeNos", routeNos);
        return "board/busModifySchedule";
    }

    // 저상 버스 시간표 페이지
    @GetMapping("/low-schedule")
    public String showLowFloorSchedule(Model model) {
        List<String> routeNos = busScheduleRepository.getRouteNosWithLowBus("D");   // 스케줄 데이터가 있는 노선만 검색
        Collections.sort(routeNos);
        model.addAttribute("routeNos", routeNos);
        return "board/lowBusSchedule";
    }
}
