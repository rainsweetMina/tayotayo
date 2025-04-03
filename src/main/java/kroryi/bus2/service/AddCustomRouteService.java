package kroryi.bus2.service;

import jakarta.transaction.Transactional;
import kroryi.bus2.dto.Route.CustomRouteRegisterRequestDTO;
import kroryi.bus2.dto.Route.CustomRouteDTO;
import kroryi.bus2.dto.RouteStopLinkDTO;
import kroryi.bus2.entity.BusStop;
import kroryi.bus2.entity.CustomRoute;

import kroryi.bus2.entity.RouteStopLink;
import kroryi.bus2.repository.jpa.AddRouteStopLinkRepository;
import kroryi.bus2.repository.jpa.BusStopRepository;
import kroryi.bus2.repository.jpa.route.CustomRouteRepository;
import kroryi.bus2.repository.jpa.route.RouteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AddCustomRouteService {

    private final CustomRouteRepository customRouteRepository;
    private final RouteRepository routeRepository;
    private final AddRouteStopLinkRepository routeStopLinkRepository;
    private final BusStopRepository busStopRepository;

    @Transactional
    public void saveFullRoute(CustomRouteRegisterRequestDTO request) {
        CustomRouteDTO routeDto = request.getRoute();
        String routeId = routeDto.getRouteId();
        String stBsId = routeDto.getStBsId();
        String edBsId = routeDto.getEdBsId();

        // 노선 ID 유효성 검사 (null 또는 빈 문자열)
        if (routeId == null || routeId.trim().isEmpty()) {
            throw new IllegalArgumentException("노선 ID는 비어 있을 수 없습니다.");
        }

        // routeId 중복 검사 (커스텀 + 기존 노선 테이블)
        if (customRouteRepository.existsByRouteId(routeId) || routeRepository.existsByRouteId(routeId)) {
            throw new IllegalArgumentException("이미 등록된 노선 ID입니다: " + routeId);
        }

        // 출발/도착 정류소 존재 여부 검사
        if (!busStopRepository.existsByBsId(routeDto.getStBsId())) {
            throw new IllegalArgumentException("출발 정류소가 존재하지 않습니다: " + routeDto.getStBsId());
        }
        if (!busStopRepository.existsByBsId(routeDto.getEdBsId())) {
            throw new IllegalArgumentException("도착 정류소가 존재하지 않습니다: " + routeDto.getEdBsId());
        }

        // 출/도착 정류소가 같으면 안 됨
        if (routeDto.getStBsId().equals(routeDto.getEdBsId())) {
            throw new IllegalArgumentException("출발 정류소와 도착 정류소는 같을 수 없습니다.");
        }

        // 경유 정류장 중복 검사
        Set<String> bsIdSet = new HashSet<>();
        for (RouteStopLinkDTO stop : request.getStops()) {
            if (!bsIdSet.add(stop.getBsId())) {
                throw new IllegalArgumentException("경유 정류장에 중복된 정류소가 포함되어 있습니다: " + stop.getBsId());
            }

            // 경유지에 출/도착지 포함 여부
            if (stop.getBsId().equals(stBsId) || stop.getBsId().equals(edBsId)) {
                throw new IllegalArgumentException("경유 정류장에 출발지 또는 도착지 정류소가 포함되어 있습니다: " + stop.getBsId());
            }
        }

        // 1. 노선 저장
        CustomRoute route = CustomRoute.builder()
                .routeId(routeDto.getRouteId())
                .routeNo(routeDto.getRouteNo())
                .stBsId(routeDto.getStBsId())
                .edBsId(routeDto.getEdBsId())
                .stNm(routeDto.getStNm())
                .edNm(routeDto.getEdNm())
                .routeNote(routeDto.getRouteNote())
                .dataconnareacd(routeDto.getDataconnareacd())
                .dirRouteNote(routeDto.getDirRouteNote())
                .ndirRouteNote(routeDto.getNdirRouteNote())
                .routeTCd(routeDto.getRouteTCd())
                .build();

        customRouteRepository.save(route);

        // 2. 정류장 좌표 포함해서 링크 데이터 생성
        List<RouteStopLink> linkList = new ArrayList<>();

        for (RouteStopLinkDTO stopDTO : request.getStops()) {
            BusStop stop = busStopRepository.findByBsId(stopDTO.getBsId())
                    .orElseThrow(() -> new IllegalArgumentException("해당 정류소가 존재하지 않습니다: " + stopDTO.getBsId()));

            RouteStopLink link = RouteStopLink.builder()
                    .routeId(routeDto.getRouteId())
                    .bsId(stop.getBsId())
                    .seq(stopDTO.getSeq())
                    .moveDir(stopDTO.getMoveDir())
                    .xPos(stop.getXPos())  // 자동 주입
                    .yPos(stop.getYPos())  // 자동 주입
                    .build();

            linkList.add(link);
        }

        routeStopLinkRepository.saveAll(linkList);
    }
}
