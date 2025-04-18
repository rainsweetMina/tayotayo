package kroryi.bus2.service.route;

import jakarta.transaction.Transactional;
import kroryi.bus2.entity.route.Route;
import kroryi.bus2.entity.route.RouteDelete;
import kroryi.bus2.entity.route.RouteStopLink;
import kroryi.bus2.repository.jpa.AddRouteStopLinkRepository;
import kroryi.bus2.repository.jpa.board.RouteStopLinkRepository;
import kroryi.bus2.repository.jpa.route.RouteDeleteRepository;
import kroryi.bus2.repository.jpa.route.RouteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Log4j2
@RequiredArgsConstructor
public class DeleteRouteService {


    private final AddRouteStopLinkRepository addrouteStopLinkRepository;
    private final RouteRepository routeRepository;
    private final RouteDeleteRepository routeDeleteRepository;

    @Transactional
    public void deleteRoute(String routeId) {
        log.info("노선 삭제 요청 - routeId: {}", routeId);

        addrouteStopLinkRepository.deleteByRouteId(routeId);
        log.info("노선 링크 삭제 완료");

        routeRepository.deleteByRouteId(routeId);
        log.info("노선 엔티티 삭제 완료");
    }

    @Transactional
    public void backupRoute(String routeId) {
        // 1. 노선 존재 여부 확인
        Route route = routeRepository.findByRouteId(routeId)
                .orElseThrow(() -> new IllegalArgumentException("노선 ID가 존재하지 않습니다: " + routeId));

        // 2. 백업 엔티티 생성
        RouteDelete backup = RouteDelete.builder()
                .routeId(route.getRouteId())
                .routeNo(route.getRouteNo())
                .stBsId(route.getStBsId())
                .edBsId(route.getEdBsId())
                .stNm(route.getStNm())
                .edNm(route.getEdNm())
                .routeNote(route.getRouteNote())
                .dataconnareacd(route.getDataconnareacd())
                .dirRouteNote(route.getDirRouteNote())
                .ndirRouteNote(route.getNdirRouteNote())
                .routeTCd(route.getRouteTCd())
                .deletedAt(LocalDateTime.now())
                .build();

        System.out.println("backup: " + backup);
        try {
            routeDeleteRepository.save(backup);
        } catch (Exception e) {
            log.error("❌ 백업 저장 실패", e);
        }
    }

}
