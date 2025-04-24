package kroryi.bus2.controller.bus;


import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kroryi.bus2.dto.busSetting.PathSettingDTO;
import kroryi.bus2.repository.jpa.board.RouteStopLinkRepository;
import kroryi.bus2.repository.jpa.bus_stop.BusStopRepository;
import kroryi.bus2.repository.jpa.route.RouteRepository;
import kroryi.bus2.service.BusInfoInitService;
import kroryi.bus2.service.BusRouteRealTimeDataService;
import kroryi.bus2.service.busSetting.PathSettingService;
import kroryi.bus2.service.busStop.BusStopDataService;
import kroryi.bus2.service.route.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Set;

@Tag(name = "버스-길찾기-정보-설정", description = "")
@RestController
@RequestMapping("/api/bus")
@RequiredArgsConstructor
@Log4j2
public class BusSettingController {

    private final PathSettingService pathSettingService;

    @Operation(summary = "버스 경로 탐색 반경 및 시간 가중치 수정", description = "시작 반경, 도착 반경, 시간 가중치를 수정합니다.")
    @PostMapping("/path-settings")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateSettings(@RequestBody PathSettingDTO dto) {
        pathSettingService.updateSearchDistances(dto.getStartDistance(), dto.getEndDistance(), dto.getTimeFactor());
        System.out.println("현재 적용된 탐색반경 : " + dto.getStartDistance() + "," + dto.getEndDistance());
        return ResponseEntity.ok("✅ 거리 설정이 갱신되었습니다");
    }

    @Operation(summary = "현재 설정된 탐색 반경 및 시간 가중치 조회", description = "설정된 시작 반경, 도착 반경, 시간 가중치 정보를 조회합니다.")
    @GetMapping("/path-settings")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PathSettingDTO> getCurrentSettings() {
        PathSettingDTO dto = new PathSettingDTO();
        dto.setStartDistance(pathSettingService.getStartRadius());
        dto.setEndDistance(pathSettingService.getEndRadius());
        dto.setTimeFactor(pathSettingService.getTimeFactor());
        return ResponseEntity.ok(dto);
    }


}
