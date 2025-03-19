package kroryi.bus2.controller;

import kroryi.bus2.repository.BusStopRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/")
@RequiredArgsConstructor
public class BusController {

    @GetMapping("/bus")
    public String getBusStops(Model model) {
        return "busStops";
    }
}
