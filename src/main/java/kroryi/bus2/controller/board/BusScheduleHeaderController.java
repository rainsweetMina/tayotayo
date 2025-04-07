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
        List<Integer> stopOrder = (List<Integer>) body.get("stopOrder");

        busScheduleHeaderService.saveStopOrder(routeId, stopOrder);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/schedule-header")
    public ResponseEntity<List<Integer>> getHeader(@RequestParam String routeId) throws JsonProcessingException {
        return ResponseEntity.ok(busScheduleHeaderService.getStopOrder(routeId));
    }
}
