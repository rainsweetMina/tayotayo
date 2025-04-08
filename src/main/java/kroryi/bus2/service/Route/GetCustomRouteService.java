package kroryi.bus2.service.Route;

import kroryi.bus2.dto.coordinate.CoordinateDTO;
import kroryi.bus2.entity.RouteStopLink;
import kroryi.bus2.repository.jpa.AddRouteStopLinkRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Log4j2
@RequiredArgsConstructor
public class GetCustomRouteService {

    private final AddRouteStopLinkRepository routeStopLinkRepository;
    private final RouteDataService routeDataService;

    public List<RouteStopLink> getByRouteId(String routeId) {
        return routeStopLinkRepository.findByRouteId(routeId);
    }

    public List<Map<String, Object>> getBusRoute(String routeId) throws IOException {
        List<RouteStopLink> stops = getByRouteId(routeId)
                .stream()
                .sorted(Comparator.comparingInt(RouteStopLink::getSeq))  // ✅ seq 기준 정렬
                .toList();

        return stops.stream().map(stop -> {
            Map<String, Object> map = new HashMap<>();
            map.put("bsId", stop.getBsId());
            map.put("bsNm", stop.getBusStop() != null ? stop.getBusStop().getBsNm() : null);
            map.put("xPos", stop.getXPos());
            map.put("yPos", stop.getYPos());
            map.put("moveDir", stop.getMoveDir());
            map.put("seq", stop.getSeq());
            return map;
        }).collect(Collectors.toList());
    }



    // moveDir 기준으로 나눠서 CoordinateDTO로 변환해서 반환
    public Map<String, List<CoordinateDTO>> getCoordinatesByRouteIdGrouped(String routeId) {
        List<RouteStopLink> links = getByRouteId(routeId)
                .stream()
                .sorted(Comparator.comparingInt(RouteStopLink::getSeq))
                .toList();

        Map<String, List<CoordinateDTO>> grouped = new HashMap<>();

        List<CoordinateDTO> forward = new ArrayList<>();
        List<CoordinateDTO> reverse = new ArrayList<>();

        for (RouteStopLink link : links) {
            CoordinateDTO coord = new CoordinateDTO(link.getXPos(), link.getYPos());
            if ("1".equals(link.getMoveDir())) {
                forward.add(coord);
            } else if ("0".equals(link.getMoveDir())) {
                reverse.add(coord);
            }
        }

        grouped.put("forward", forward);
        grouped.put("reverse", reverse);
        return grouped;
    }
    public List<CoordinateDTO> getChunkedOrsCustom(List<CoordinateDTO> points) throws InterruptedException {
        List<CoordinateDTO> coordinates = points.stream()
                .map(p -> new CoordinateDTO(p.getXPos(), p.getYPos()))
                .collect(Collectors.toList());
        List<CoordinateDTO> result = new ArrayList<>();
        for (int i = 0; i < coordinates.size() - 1; i += 69) {
            int toIndex = Math.min(i + 70, coordinates.size());
            List<CoordinateDTO> chunk = coordinates.subList(i, toIndex);

            try {
                result.addAll(routeDataService.getOrsPath(chunk));
            } catch (IOException e) {
                log.warn("🚫 ORS 요청 실패 → fallback으로 직선 연결: {}", chunk);
                result.addAll(chunk); // 🔁 그냥 직선 연결
            }
        }
        return result;
    }
}
