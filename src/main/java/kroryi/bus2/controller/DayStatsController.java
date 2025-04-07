package kroryi.bus2.controller;

import kroryi.bus2.service.DayStatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/stats")
public class DayStatsController {

    @Autowired
    private DayStatsService dayStatsService;


    @PostMapping(value = "/request", produces = "application/json")
    public ResponseEntity<Map<String, String>> incrementRequest() {
        dayStatsService.incrementRequestCount();
        Map<String, String> response = Map.of("message", "Request count incremented!");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/search")
    public String incrementSearch() {
        dayStatsService.incrementSearchCount();
        return "Search count incremented!";
    }

    @GetMapping("/request")
    public long getRequestCount() {
        return dayStatsService.getRequestCount();
    }

    @GetMapping("/search")
    public long getSearchCount() {
        return dayStatsService.getSearchCount();
    }

}
