package kroryi.bus2.controller;

import kroryi.bus2.service.BusLocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/bus")
//@RequiredArgsConstructor
public class BusController {

    @Autowired
    private BusLocationService busLocationService;

    @GetMapping("/location/{routeId}")
    public ResponseEntity<?> getBusLocation(@PathVariable String routeId) {
        System.out.println(routeId);
        return ResponseEntity.ok(busLocationService.fetchAndSaveBusLocation(routeId));
    }
}
