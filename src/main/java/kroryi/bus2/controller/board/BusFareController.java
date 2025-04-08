package kroryi.bus2.controller.board;

import kroryi.bus2.entity.BusFare;
import kroryi.bus2.repository.jpa.board.BusFareRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class BusFareController {
    private final BusFareRepository busFareRepository;

    @GetMapping("/fare")
    public String showBusFare(Model model) {
        List<BusFare> fareList = busFareRepository.findAll();
        model.addAttribute("fares", fareList);
        return "/board/busFare";
    }

}
