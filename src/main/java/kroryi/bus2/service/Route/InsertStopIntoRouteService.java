package kroryi.bus2.service.Route;

import jakarta.transaction.Transactional;
import kroryi.bus2.dto.RouteStopLinkDTO;
import kroryi.bus2.entity.bus_stop.BusStop;
import kroryi.bus2.entity.RouteStopLink;
import kroryi.bus2.repository.jpa.BusStopRepository;
import kroryi.bus2.repository.jpa.board.RouteStopLinkRepository;
import kroryi.bus2.repository.jpa.route.RouteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InsertStopIntoRouteService {

    private final RouteStopLinkRepository routeStopLinkRepository;
    private final BusStopRepository busStopRepository;
    private final RouteRepository RouteRepository;


    @Transactional
    public void insertStopIntoRoute(RouteStopLinkDTO dto) {
        String routeId = dto.getRouteId();
        String moveDir = dto.getMoveDir();
        int insertSeq = dto.getSeq();

        //  출발 정류소(seq == 1)는 변경 불가
        if ("1".equals(moveDir) && insertSeq == 1) {
            throw new IllegalArgumentException("출발 정류소 바로 앞에는 정류소를 추가할 수 없습니다.");
        }

        //  1. 삽입 위치 이후의 정류장 seq +1
        List<RouteStopLink> toShift = routeStopLinkRepository
                .findByRouteIdAndMoveDir(routeId, moveDir)
                .stream()
                .filter(stop -> stop.getSeq() >= insertSeq)
                .toList();

        toShift.forEach(stop -> stop.setSeq(stop.getSeq() + 1));
        routeStopLinkRepository.saveAll(toShift);

        //  2. 정류소 존재 확인 및 좌표 가져오기
        BusStop stop = busStopRepository.findByBsId(dto.getBsId())
                .orElseThrow(() -> new IllegalArgumentException("해당 정류소 ID가 존재하지 않습니다: " + dto.getBsId()));

        //  3. 새 정류소 삽입
        RouteStopLink newStop = RouteStopLink.builder()
                .routeId(routeId)
                .bsId(stop.getBsId())
                .seq(insertSeq)
                .moveDir(moveDir)
                .xPos(stop.getXPos())
                .yPos(stop.getYPos())
                .build();

        routeStopLinkRepository.save(newStop);

        //  4. 정방향일 경우 도착지 자동 갱신
        if ("1".equals(moveDir)) {
            int maxSeq = routeStopLinkRepository.findMaxSeqByRouteIdAndMoveDir(routeId, "1");

            // 새로 넣은 seq가 가장 크다면 → 도착지 변경
            if (insertSeq >= maxSeq) {
                RouteRepository.findByRouteId(routeId).ifPresent(route -> {
                    route.setEdBsId(stop.getBsId());
                    route.setEdNm(stop.getBsNm());
                    RouteRepository.save(route);
                });
            }
        }
    }

}