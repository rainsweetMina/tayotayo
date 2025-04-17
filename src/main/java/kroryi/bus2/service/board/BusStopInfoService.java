package kroryi.bus2.service.board;

import kroryi.bus2.aop.AdminAudit;
import kroryi.bus2.entity.busStop.BusStopInfo;
import kroryi.bus2.repository.jpa.board.BusStopInfoRepository;
import kroryi.bus2.repository.jpa.board.RouteStopLinkRepository;
import kroryi.bus2.repository.jpa.route.RouteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BusStopInfoService {
    private final BusStopInfoRepository busStopInfoRepository;
    private final RouteStopLinkRepository routeStopLinkRepository;
    private final RouteRepository routeRepository;

    // 웹 드롭다운에 사용할 구(주소) 목록 조회
    public List<String> getAllDistricts() {
        return busStopInfoRepository.findDistinctDistrict();
    }

    // 웹 드롭다운에 사용할 동(주소) 목록 조회
    public List<String> getNeighborhoodsByDistrict(String district) {
        return busStopInfoRepository.findNeighborhoodsByDistrict(district);
    }

    // 정류장 목록 조회(구)
    public List<BusStopInfo> getBusStopsByDistrict(String district) {
        return busStopInfoRepository.findByDistrict(district);
    }

    // 정류장 목록 조회(구&동)
    public List<BusStopInfo> getBusStopsByDistrictAndNeighborhood(String district, String neighborhood) {
        return busStopInfoRepository.findByDistrictAndNeighborhood(district, neighborhood);
    }

    // 웹 드롭다운에 사용할 노선 조회
    public List<String> getRouteNosByType(String type) {
        List<String> all = routeRepository.findDistinctRouteNos();
        return all.stream()
                .filter(no -> switch (type) {
                    case "순환" -> no.startsWith("순환");
                    case "급행" -> no.startsWith("급행");
                    case "간선" -> no.matches("^\\d+$");
                    case "지선" -> no.matches("^[^\\d]+\\d+$");
                    default -> false;
                })
                .sorted()
                .toList();
    }

    // 정류장 목록 조회(노선)
    public List<BusStopInfo> getStopsByRouteNo(String routeNo) {
        return busStopInfoRepository.findByRouteNo(routeNo);
    }

}
