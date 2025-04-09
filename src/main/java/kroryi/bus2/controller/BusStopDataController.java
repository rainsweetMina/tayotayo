package kroryi.bus2.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.Operation;
import kroryi.bus2.dto.busStop.BusStopDTO;
import kroryi.bus2.dto.busStop.BusStopUpdateDTO;
import kroryi.bus2.entity.BusStop;
import kroryi.bus2.entity.CustomRoute;
import kroryi.bus2.entity.Route;
import kroryi.bus2.service.BusStop.AddBusStopService;
import kroryi.bus2.service.BusStop.BusStopDataService;
import kroryi.bus2.service.BusStop.UpdateBusStopService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bus")
@RequiredArgsConstructor
@Log4j2
public class BusStopDataController {

    private final AddBusStopService addBusStopService;
    private final UpdateBusStopService updateBusStopService;
    private final BusStopDataService busStopDataService;

    @PostMapping("/addBusStop")
    public ResponseEntity<BusStop> addStop(@RequestBody BusStopDTO dto) {
        BusStop created = addBusStopService.createBusStop(dto);
        return ResponseEntity.ok(created);
    }

    // ✅ 정류장 ID로 조회 (RequestParam 버전)
    @GetMapping("/busStop")
    public ResponseEntity<BusStop> getBusStopById(@RequestParam String bsId) {
        log.info("[GET] /busStop?keyword={} 호출", bsId);
        BusStop stop = busStopDataService.getBusStopById(bsId);
        return ResponseEntity.ok(stop);
    }

    @PutMapping("/updateStop/{bsId}")
    public ResponseEntity<String> updateBusStop(
            @PathVariable String bsId,
            @RequestBody BusStopUpdateDTO dto) {
        updateBusStopService.updateBusStop(bsId, dto);
        return ResponseEntity.ok("정류장 정보가 수정되었습니다.");
    }

    @Operation(summary = "정류장 검색", description = "사용자가 검색창에 키워드를 입력했을 때, 해당 키워드에 해당하는 정류장명 반환")
    @GetMapping(value = "/searchBS", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getBusNames(@RequestParam String keyword) {
        List<BusStop> busStop = busStopDataService.getBusStopsByNm(keyword);
        return ResponseEntity.ok(busStop);
    }
}
