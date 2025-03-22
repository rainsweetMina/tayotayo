package kroryi.bus2.service;

import kroryi.bus2.dto.BusStopDTO;
import kroryi.bus2.dto.RouteDTO;
import kroryi.bus2.entity.BusStop;
import kroryi.bus2.entity.Route;
import kroryi.bus2.repository.BusStopRepository;
import kroryi.bus2.repository.RouteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RouteDataService {

    private final RouteRepository routeRepository;

    public List<String> getBusByNm(String routeNo) {
        return routeRepository.searchByRouteNumber(routeNo);
    }



}
