package kroryi.bus2.service.Route;

import jakarta.transaction.Transactional;
import kroryi.bus2.repository.jpa.AddRouteStopLinkRepository;
import kroryi.bus2.repository.jpa.route.RouteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

@Service
@Log4j2
@RequiredArgsConstructor
public class DeleteRouteService {


    private final AddRouteStopLinkRepository routeStopLinkRepository;
    private final RouteRepository routeRepository;

    @Transactional
    public void deleteRoute(String routeId) {
        log.info("노선 삭제 요청 - routeId: {}", routeId);

        routeStopLinkRepository.deleteByRouteId(routeId);
        log.info("노선 링크 삭제 완료");

        routeRepository.deleteByRouteId(routeId);
        log.info("노선 엔티티 삭제 완료");
    }

}
