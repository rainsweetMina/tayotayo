package kroryi.bus2.service.route;

import kroryi.bus2.dto.Route.RouteResultDTO;
import kroryi.bus2.dto.busStop.BusStopDTO;
import kroryi.bus2.dto.coordinate.CoordinateDTO;
import kroryi.bus2.entity.busStop.BusStop;
import kroryi.bus2.entity.route.Route;
import kroryi.bus2.entity.route.RouteStopLink;
import kroryi.bus2.repository.jpa.board.RouteStopLinkRepository;
import kroryi.bus2.repository.jpa.bus_stop.BusStopRepository;
import kroryi.bus2.repository.jpa.route.RouteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@Log4j2
@RequiredArgsConstructor
public class RouteFinder {

    private final RouteDataService routeDataService;
    private final RouteStopLinkRepository routeStopLinkRepository;
    private final RouteRepository routeRepository;
    private final BusStopRepository busStopRepository;

    public List<RouteResultDTO> findRoutes(String startBsId, String endBsId) {
        List<String> sharedRouteIds = routeStopLinkRepository.findDirectRouteIdsByStartAndEnd(startBsId, endBsId);

        List<RouteResultDTO> results = new ArrayList<>();
        for (String routeId : sharedRouteIds) {
            Optional<RouteStopLink> startLink = routeStopLinkRepository.findByRouteIdAndBsId(routeId, startBsId);
            Optional<RouteStopLink> endLink = routeStopLinkRepository.findByRouteIdAndBsId(routeId, endBsId);

            if (startLink.isEmpty() || endLink.isEmpty()) continue;

            int startSeq = startLink.get().getSeq();
            int endSeq = endLink.get().getSeq();
            int minSeq = Math.min(startSeq, endSeq);
            int maxSeq = Math.max(startSeq, endSeq);

// 여기만 교체됨 ✅
            List<RouteStopLink> linkList = routeStopLinkRepository
                    .findByRouteIdAndSeqBetween(routeId, minSeq, maxSeq);

            if (linkList.isEmpty()) continue;

            List<BusStopDTO> stops = linkList.stream()
                    .map(link -> {
                        return busStopRepository.findByBsId(link.getBsId())
                                .map(BusStopDTO::fromEntity)
                                .orElse(null); // 또는 에러 던지기
                    })
                    .filter(Objects::nonNull)
                    .toList();

            Route route = routeRepository.findByRouteId(routeId)
                    .orElseThrow(() -> new RuntimeException("routeId에 해당하는 노선 없음: " + routeId));

            List<CoordinateDTO> stationCoords = linkList.stream()
                    .map(link -> {
                        BusStop stop = busStopRepository.findByBsId(link.getBsId()).orElse(null);
                        return stop != null ? new CoordinateDTO(stop.getXPos(), stop.getYPos()) : null;
                    })
                    .filter(Objects::nonNull)
                    .toList();

            List<CoordinateDTO> orsPath;
            try {
                orsPath = routeDataService.getOrsPath(stationCoords);
                System.out.println(orsPath + "--------------------------------------");
            } catch (IOException | InterruptedException e) {
                log.warn("🚫 ORS 경로 계산 실패: {}", e.getMessage());
                orsPath = stationCoords; // 🔁 fallback: 직선 연결
            }

            RouteResultDTO result = RouteResultDTO.builder()
                    .type("직통")
                    .routeId(routeId)
                    .routeNo(route.getRouteNo())
                    .startBsId(startBsId)
                    .endBsId(endBsId)
                    .transferCount(0)
                    .transferStationId(null)
                    .transferStationName(null)
                    .estimatedMinutes(linkList.size() * 2)
                    .stationIds(stops)
                    .orsPath(orsPath) // 🔥 여기에 주입
                    .build();

            results.add(result);
        }

        return results;
    }


}
