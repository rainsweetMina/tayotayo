package kroryi.bus2.service.Route;

import jakarta.transaction.Transactional;
import kroryi.bus2.entity.RouteStopLink;
import kroryi.bus2.repository.jpa.AddRouteStopLinkRepository;
import kroryi.bus2.repository.jpa.board.RouteStopLinkRepository;
import kroryi.bus2.repository.jpa.route.CustomRouteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Log4j2
@RequiredArgsConstructor
public class DeleteCustomRouteService {

    private final CustomRouteRepository customRouteRepository;
    private final AddRouteStopLinkRepository routeStopLinkRepository;

    @Transactional
    public void deleteCustomRoute(String routeId) {

        routeStopLinkRepository.deleteByRouteId(routeId);
        customRouteRepository.deleteByRouteId(routeId);
    }

}
