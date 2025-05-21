package kroryi.bus2.controller.bus;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import kroryi.bus2.dto.BusRealtimeDTO;
import kroryi.bus2.dto.Route.CustomRouteRegisterRequestDTO;
import kroryi.bus2.dto.Route.RouteDTO;
import kroryi.bus2.dto.Route.RouteListDTO;
import kroryi.bus2.dto.Route.RouteResultDTO;
import kroryi.bus2.dto.RouteStopLinkDTO;
import kroryi.bus2.dto.busStop.BusStopDTO;
import kroryi.bus2.dto.coordinate.CoordinateDTO;
import kroryi.bus2.entity.busStop.BusStop;
import kroryi.bus2.entity.route.Route;
import kroryi.bus2.repository.jpa.board.RouteStopLinkRepository;
import kroryi.bus2.repository.jpa.bus_stop.BusStopRepository;
import kroryi.bus2.repository.jpa.route.RouteRepository;
import kroryi.bus2.service.BusInfoInitService;
import kroryi.bus2.service.BusRouteRealTimeDataService;
import kroryi.bus2.service.busStop.BusStopDataService;
import kroryi.bus2.service.route.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Tag(name = "버스-노선-정보 ", description = "")
@RestController
@RequestMapping("/api/bus")
@RequiredArgsConstructor
@Log4j2
// 버스,노선 관련 데이터를 클라이언트에 제공하는 REST API 컨트롤러
// 이 컨트롤러는 JSON 형식으로 데이터를 반환하며, 클라이언트(웹/앱)에서 실시간 정보 조회 및 검색에 활용
public class BusRouteDataController {
    private final BusInfoInitService busInfoInitService;
    private final BusStopDataService busStopDataService;
    private final RouteDataService routeDataService;
    private final ObjectMapper objectMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final BusRouteRealTimeDataService busRouteRealTimeDataService;
    private final GetRouteLinkService getRouteLinkService;
    private final BusStopRepository busStopRepository;
    private final RouteRepository routeRepository;
    private final AddRouteService addRouteService;
    private final AddRouteStopLinkService addRouteStopLinkService;
    private final RouteStopLinkRepository routeStopLinkRepository;
    private final InsertStopIntoRouteService insertStopIntoRouteService;
    private final DeleteStopFromRouteService deleteStopFromRouteService;
    private final DeleteRouteService deleteRouteService;
    private final RouteFinderService routeFinderService;


    @Value("${api.service-key-decoding}")
    private String serviceKey;

    // 페이징 + 검색이 추가된 전체 노선 게시판
    @Operation(summary = "전체 노선 불러오기", description = "페이징 + 검색이 추가된 전체 노선 게시판")
    @GetMapping("/routes")
    public ResponseEntity<Page<RouteListDTO>> getRoutes(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "asc") String sort // 🔽 asc 또는 desc
    ) {
        Page<RouteListDTO> result = routeDataService.getRoutesWithPaging(keyword, page, size, sort);
        return ResponseEntity.ok(result);
    }

    // 경유 정류소만 추가 거의 쓸일없을듯?
    @Hidden
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "경유지 추가", description = "새로운 경유 정류소만 추가 거의 쓸일없을듯?")
    @PostMapping("/AddRouteStopLink")
    public void addRouteStopLink(@RequestBody List<RouteStopLinkDTO> dtoList) {
        System.out.println("받아온 데이터 : " + dtoList);
        addRouteStopLinkService.saveAll(dtoList);
    }

    // 노선만들기 + 경유 정류소 추가
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "노선 경유지 추가", description = "새로운 노선 경유지를 추가합니다.")
    @PostMapping("/AddBusRoute")
    public ResponseEntity<?> addRoute(@RequestBody CustomRouteRegisterRequestDTO request) {
        try {
            addRouteService.saveFullRoute(request);
            return ResponseEntity.ok(Map.of("success", true, "routeId", request.getRoute().getRouteId()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @Operation(summary = "노선정보 찾기", description = "노선ID로 노선정보 찾아줌")
    @GetMapping("/getRouteInfo")
    public ResponseEntity<RouteDTO> getRouteByRouteId(@RequestParam String routeId) {
        RouteDTO route = routeDataService.getRouteByRouteId(routeId);
        return ResponseEntity.ok(route);
    }

    // 이거 에러있음
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "노선정보 수정", description = "커스텀/일반 노선 구분 없이 노선ID로 정보 수정")
    @PutMapping("/UpdateRouteUnified/{routeId}")
    public ResponseEntity<?> updateAnyRoute(@PathVariable String routeId,
                                            @RequestBody RouteDTO updatedDto) {

        Optional<Route> normalOpt = routeRepository.findByRouteId(routeId);
        if (normalOpt.isPresent()) {
            Route route = normalOpt.get();
            route.setRouteNo(updatedDto.getRouteNo());
            route.setRouteNote(updatedDto.getRouteNote());
            route.setDataconnareacd(updatedDto.getDataconnareacd());
            route.setDirRouteNote(updatedDto.getDirRouteNote());
            route.setNdirRouteNote(updatedDto.getNdirRouteNote());
            route.setRouteTCd(updatedDto.getRouteTCd());

            routeRepository.save(route);
            return ResponseEntity.ok(Map.of("success", true, "message", "✅ 일반 노선 정보 수정 완료"));
        }

        // 둘 다 없으면 에러
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("success", false, "message", "해당 노선 ID를 찾을 수 없습니다."));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "노선링크 순서 수정", description = "노선ID로 노선링크의 순서를 수정해줌 *기존 노선엔 절대 사용금지!!!")
    @PutMapping("/UpdateRouteLink")
    public ResponseEntity<?> updateRouteSeq(@RequestBody List<RouteStopLinkDTO> dtoList) {
        if (dtoList.isEmpty()) {
            return ResponseEntity.badRequest().body("수정할 데이터가 없습니다.");
        }

        for (RouteStopLinkDTO dto : dtoList) {
            routeStopLinkRepository.findByRouteIdAndBsIdAndMoveDir(
                    dto.getRouteId(), dto.getBsId(), dto.getMoveDir()
            ).ifPresent(entity -> {
                entity.setSeq(dto.getSeq());
                routeStopLinkRepository.save(entity);
            });
        }

        return ResponseEntity.ok(Map.of("success", true, "message", "노선 순서(seq)가 성공적으로 수정되었습니다."));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "노선링크 정류소 추가", description = "노선ID로 노선링크의 정류소를 추가해줌 *기존의 노선에 새로운 정류소가 추가 될수도있으니 주의!")
    @PostMapping("/InsertStop")
    public ResponseEntity<?> insertStop(@RequestBody RouteStopLinkDTO dto) {
        try {
            insertStopIntoRouteService.insertStopIntoRoute(dto);
            return ResponseEntity.ok(Map.of("success", true, "message", "정류장 삽입 완료"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "노선링크 정류소 삭제", description = "노선링크의 정류소를 삭제해줌 *기존의 노선의 정류소도 삭제 가능하니 조심!")
    @DeleteMapping("/delete-stop")
    public ResponseEntity<?> deleteStop(@RequestParam String routeId,
                                        @RequestParam String moveDir,
                                        @RequestParam int seq) {
        try {
            deleteStopFromRouteService.deleteStopFromRoute(routeId, moveDir, seq);
            return ResponseEntity.ok("정류소 삭제 완료");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "노선 삭제", description = "노선을 삭제해줌 *기존의 노선도 삭제 가능하니 조심!")
    @DeleteMapping("/deleteRoute")
    public ResponseEntity<?> deleteRoute(@RequestParam String routeId) {
        try {
            deleteRouteService.backupRoute(routeId);
            deleteRouteService.deleteRoute(routeId);
            return ResponseEntity.ok(Map.of("success", true, "message", "노선 삭제 완료"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "삭제 실패: " + e.getMessage()));
        }
    }

}
