package kroryi.bus2.service.route;

import jakarta.transaction.Transactional;
import kroryi.bus2.dto.Route.CustomRouteRegisterRequestDTO;
import kroryi.bus2.dto.Route.CustomRouteDTO;
import kroryi.bus2.dto.RouteStopLinkDTO;
import kroryi.bus2.entity.bus_stop.BusStop;

import kroryi.bus2.entity.route.Route;
import kroryi.bus2.entity.route.RouteStopLink;
import kroryi.bus2.repository.jpa.AddRouteStopLinkRepository;
import kroryi.bus2.repository.jpa.bus_stop.BusStopRepository;
import kroryi.bus2.repository.jpa.route.RouteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AddRouteService {

    private final RouteRepository routeRepository;
    private final AddRouteStopLinkRepository routeStopLinkRepository;
    private final BusStopRepository busStopRepository;

    @Transactional
    public void saveFullRoute(CustomRouteRegisterRequestDTO request) {
        CustomRouteDTO routeDto = request.getRoute();
        String routeId = routeDto.getRouteId();
        String stBsId = routeDto.getStBsId();
        String edBsId = routeDto.getEdBsId();

        if (routeId == null || routeId.trim().isEmpty()) {
            throw new IllegalArgumentException("노선 ID는 비어 있을 수 없습니다.");
        }
        if (routeRepository.existsByRouteId(routeId)) {
            throw new IllegalArgumentException("이미 등록된 노선 ID입니다: " + routeId);
        }
        if (!busStopRepository.existsByBsId(stBsId)) {
            throw new IllegalArgumentException("출발 정류소가 존재하지 않습니다: " + stBsId);
        }
        if (!busStopRepository.existsByBsId(edBsId)) {
            throw new IllegalArgumentException("도착 정류소가 존재하지 않습니다: " + edBsId);
        }
        if (stBsId.equals(edBsId)) {
            throw new IllegalArgumentException("출발 정류소와 도착 정류소는 같을 수 없습니다.");
        }

        Set<String> forwardBsIdSet = new HashSet<>();
        for (RouteStopLinkDTO stop : request.getStopsForward()) {
            if (!forwardBsIdSet.add(stop.getBsId())) {
                throw new IllegalArgumentException("정방향 경유 정류장에 중복이 있습니다: " + stop.getBsId());
            }
            if (stop.getBsId().equals(stBsId) || stop.getBsId().equals(edBsId)) {
                throw new IllegalArgumentException("출/도착 정류장이 경유지에 포함되어 있습니다: " + stop.getBsId());
            }
        }

        Set<String> backwardBsIdSet = new HashSet<>();
        for (RouteStopLinkDTO stop : request.getStopsBackward()) {
            if (!backwardBsIdSet.add(stop.getBsId())) {
                throw new IllegalArgumentException("역방향 경유 정류장에 중복이 있습니다: " + stop.getBsId());
            }
        }

        Route route = Route.builder()
                .routeId(routeId)
                .routeNo(routeDto.getRouteNo())
                .stBsId(stBsId)
                .edBsId(edBsId)
                .stNm(routeDto.getStNm())
                .edNm(routeDto.getEdNm())
                .routeNote(routeDto.getRouteNote())
                .dataconnareacd(routeDto.getDataconnareacd())
                .dirRouteNote(routeDto.getDirRouteNote())
                .ndirRouteNote(routeDto.getNdirRouteNote())
                .routeTCd(routeDto.getRouteTCd())
                .build();

        routeRepository.save(route);

        // 정방향 저장 (moveDir = "1")
        List<RouteStopLink> forwardLinks = new ArrayList<>();
        int seq = 1;

        BusStop stStop = busStopRepository.findByBsId(stBsId)
                .orElseThrow(() -> new IllegalArgumentException("출발 정류소 정보 조회 실패: " + stBsId));
        forwardLinks.add(RouteStopLink.builder()
                .routeId(routeId)
                .bsId(stBsId)
                .seq(seq++)
                .moveDir("1")
                .xPos(stStop.getXPos())
                .yPos(stStop.getYPos())
                .build());

        for (RouteStopLinkDTO dto : request.getStopsForward()) {
            BusStop stop = busStopRepository.findByBsId(dto.getBsId())
                    .orElseThrow(() -> new IllegalArgumentException("정방향 경유 정류소 정보 조회 실패: " + dto.getBsId()));

            forwardLinks.add(RouteStopLink.builder()
                    .routeId(routeId)
                    .bsId(stop.getBsId())
                    .seq(seq++)
                    .moveDir("1")
                    .xPos(stop.getXPos())
                    .yPos(stop.getYPos())
                    .build());
        }

        BusStop edStop = busStopRepository.findByBsId(edBsId)
                .orElseThrow(() -> new IllegalArgumentException("도착 정류소 정보 조회 실패: " + edBsId));
        forwardLinks.add(RouteStopLink.builder()
                .routeId(routeId)
                .bsId(edStop.getBsId())
                .seq(seq)
                .moveDir("1")
                .xPos(edStop.getXPos())
                .yPos(edStop.getYPos())
                .build());

        // 역방향 저장 (moveDir = "0")
        List<RouteStopLink> backwardLinks = new ArrayList<>();
        int backSeq = 1;

        for (RouteStopLinkDTO dto : request.getStopsBackward()) {
            BusStop stop = busStopRepository.findByBsId(dto.getBsId())
                    .orElseThrow(() -> new IllegalArgumentException("역방향 경유 정류소 정보 조회 실패: " + dto.getBsId()));

            backwardLinks.add(RouteStopLink.builder()
                    .routeId(routeId)
                    .bsId(stop.getBsId())
                    .seq(backSeq++)
                    .moveDir("0")
                    .xPos(stop.getXPos())
                    .yPos(stop.getYPos())
                    .build());
        }

        routeStopLinkRepository.saveAll(forwardLinks);
        routeStopLinkRepository.saveAll(backwardLinks);
    }


}
