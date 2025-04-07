package kroryi.bus2.controller.board;

import com.fasterxml.jackson.core.JsonProcessingException;
import kroryi.bus2.service.board.BusScheduleHeaderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class BusScheduleHeaderController {

    private final BusScheduleHeaderService busScheduleHeaderService;

    @PostMapping("/schedule-header")
    public ResponseEntity<Void> saveHeader(@RequestBody Map<String, Object> body) throws JsonProcessingException {
        String routeId = (String) body.get("routeId");
        String moveDir = (String) body.get("moveDir");
        List<Integer> stopOrder = (List<Integer>) body.get("stopOrder");

        busScheduleHeaderService.saveStopOrder(routeId, moveDir, stopOrder);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/schedule-header")
    public ResponseEntity<List<Integer>> getHeader(
            @RequestParam String routeId,
            @RequestParam(required = false) String moveDir) {
        return ResponseEntity.ok(busScheduleHeaderService.getStopOrder(routeId, moveDir));
    }
}
