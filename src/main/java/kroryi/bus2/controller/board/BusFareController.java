package kroryi.bus2.controller.board;

import io.swagger.v3.oas.annotations.Operation;
import kroryi.bus2.dto.board.BusFareDTO;
import kroryi.bus2.entity.BusFare;
import kroryi.bus2.repository.jpa.board.BusFareRepository;
import kroryi.bus2.service.board.BusFareService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class BusFareController {
    private final BusFareRepository busFareRepository;
    private final BusFareService busFareService;

    @GetMapping("/fare")
    public String showBusFare(Model model) {
        List<BusFare> fareList = busFareRepository.findAll();
        model.addAttribute("fares", fareList);
        return "/board/busFare";
    }

    @GetMapping("/api/fares")
    @ResponseBody
    @Operation(summary = "버스 요금 전체 조회", description = "모든 버스 요금 정보를 조회합니다.")
    public List<BusFare> getFares() {
        return busFareService.findAll();
    }

    @PostMapping("/api/fares")
    @ResponseBody
    @Operation(summary = "버스 요금 등록", description = "새로운 버스 요금 정보를 등록합니다. /" +
            " busType = G(일반), E(급행), D(직행) / payType = card, cash")
    public ResponseEntity<BusFare> createFare(@RequestBody BusFareDTO dto) {
        return ResponseEntity.ok(busFareService.save(dto));
    }

    @PutMapping("/api/fares/{id}")
    @ResponseBody
    @Operation(summary = "버스 요금 수정", description = "기존 버스 요금 정보를 수정합니다.")
    public ResponseEntity<BusFare> updateFare(@PathVariable int id, @RequestBody BusFareDTO dto) {
        return ResponseEntity.ok(busFareService.update(id, dto));
    }

    @DeleteMapping("/api/fares/{id}")
    @ResponseBody
    @Operation(summary = "버스 요금 삭제", description = "ID로 해당 버스 요금 정보를 삭제합니다.")
    public ResponseEntity<String> deleteFare(@PathVariable int id) {
        busFareService.deleteById(id);
        return ResponseEntity.ok("삭제 완료");
    }
}
