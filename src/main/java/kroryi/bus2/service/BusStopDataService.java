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

import java.awt.print.Pageable;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BusStopDataService {

    private final BusStopRepository busStopRepository;

    public List<BusStopDTO> getAllBusStops() {
        System.out.print("서비스 응답");
        PageRequest pageRequest = PageRequest.of(0, 5);
//        System.out.printf(busStopRepository.findBusStops(pageRequest).toString());
        return busStopRepository.findBusStops(pageRequest).stream()
                .map(busStop -> BusStopDTO.builder()
                        .bsId(busStop.getBsId())
                        .bsNm(busStop.getBsNm())
                        .xPos(busStop.getXPos())
                        .yPos(busStop.getYPos())
                        .build()).collect(Collectors.toList());
    }

    public List<BusStop> getBusStopsByNm(String nm) {

        // 1. 기본 검색 (부분 검색)
        List<BusStop> result = busStopRepository.findByBsNmContaining(nm);
        // 2. 띄어쓰기 무시 검색
        if (result.isEmpty()) {
            result = busStopRepository.searchByBsNmIgnoreSpace(nm);
        }

        return result;
    }

}
