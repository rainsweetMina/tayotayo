package kroryi.bus2.service.route;

import kroryi.bus2.dto.Route.RouteResultDTO;
import kroryi.bus2.dto.busStop.BusStopDTO;
import kroryi.bus2.dto.busStop.TransferCandidate;
import kroryi.bus2.dto.coordinate.CoordinateDTO;
import kroryi.bus2.entity.busStop.BusStop;
import kroryi.bus2.entity.route.Route;
import kroryi.bus2.repository.jpa.board.RouteStopLinkRepository;
import kroryi.bus2.repository.jpa.bus_stop.BusStopRepository;
import kroryi.bus2.repository.jpa.route.RouteRepository;
import kroryi.bus2.service.busSetting.PathSettingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Log4j2
@Service
@RequiredArgsConstructor
public class RouteFinderService {

    private final RouteRepository routeRepository;
    private final RouteStopLinkRepository routeStopLinkRepository;
    private final BusStopRepository busStopRepository;
    private final PathSettingService pathSettingService;
    private final RouteDataService routeDataService;
    private final RouteFinder routeFinder;

    // 출,도착 정류소들의 특정 미터안의 후보를 찾는거
    public List<RouteResultDTO> findRoutesWithNearbyStart(String startBsId, String endBsId) {
        List<RouteResultDTO> result = new ArrayList<>();

        BusStop startStop = busStopRepository.findByBsId(startBsId)
                .orElseThrow(() -> new RuntimeException("출발 정류장을 찾을 수 없습니다: " + startBsId));

        BusStop endStop = busStopRepository.findByBsId(endBsId)
                .orElseThrow(() -> new RuntimeException("도착 정류장을 찾을 수 없습니다: " + endBsId));


        double startRadius = pathSettingService.getStartRadius();
        double endRadius = pathSettingService.getEndRadius();

        List<String> startCandidates = busStopRepository.findNearbyStationIdsWithGeo(
                startStop.getXPos(), startStop.getYPos(), startRadius);
        List<String> endCandidates = busStopRepository.findNearbyStationIdsWithGeo(
                endStop.getXPos(), endStop.getYPos(), endRadius);

        Set<String> visitedRouteIds = new HashSet<>();

        for (String candidateStartId : startCandidates) {
            for (String candidateEndId : endCandidates) {
                List<RouteResultDTO> directResults = findDirectRoutes(candidateStartId, candidateEndId);
                for (RouteResultDTO dto : directResults) {
                    if (!visitedRouteIds.contains(dto.getRouteId())) {
                        result.add(dto);
                        visitedRouteIds.add(dto.getRouteId());
                    }
                }
            }
        }

        return result;
    }

    public List<RouteResultDTO> findDirectRoutes(String startBsId, String endBsId) {
        List<RouteResultDTO> result = new ArrayList<>();
        List<String> routeIds = routeStopLinkRepository.findDirectRouteIdsWithSeqAndDir(startBsId, endBsId);

        for (String routeId : routeIds) {
            List<String> moveDirs = routeStopLinkRepository.findMoveDirByRouteIdAndBsId(routeId, startBsId);
            if (moveDirs.isEmpty()) continue;

            String moveDir = moveDirs.get(0);

            List<BusStopDTO> stationIds;
            try {
                stationIds = routeStopLinkRepository.findStationInfoBetweenWithDirection(
                        routeId, startBsId, endBsId, moveDir);
            } catch (Exception e) {
                System.err.println("Station 조회 실패: " + e.getMessage());
                continue;
            }

            if (stationIds.isEmpty()) continue;

            Route route = routeRepository.findByRouteId(routeId)
                    .orElseThrow(() -> new RuntimeException("노선 없음: " + routeId));

            double factor = pathSettingService.getTimeFactor();
            int estimatedMinutes = (int) Math.round(stationIds.size() * factor);

            // ✅ ORS는 비워서 전달
            result.add(RouteResultDTO.builder()
                    .type("직통")
                    .routeId(route.getRouteId())
                    .routeNo(route.getRouteNo())
                    .startBsId(startBsId)
                    .endBsId(endBsId)
                    .transferCount(0)
                    .stationIds(stationIds)
                    .estimatedMinutes(estimatedMinutes)
                    .orsPath(Collections.emptyList()) // 빈 리스트 전달
                    .build());
        }

        return result;
    }


    public List<RouteResultDTO> findRoutesWithNearbyStartTransfer(String startBsId, String endBsId) {
        List<RouteResultDTO> result = new ArrayList<>();

        BusStop startStop = busStopRepository.findByBsId(startBsId)
                .orElseThrow(() -> new RuntimeException("출발 정류장을 찾을 수 없습니다: " + startBsId));

        BusStop endStop = busStopRepository.findByBsId(endBsId)
                .orElseThrow(() -> new RuntimeException("도착 정류장을 찾을 수 없습니다: " + endBsId));

        // 후보 정류장 최대 5개로 제한
        List<String> startCandidates = busStopRepository.findNearbyStationIdsWithGeo(
                        startStop.getXPos(), startStop.getYPos(), 200.0).stream()
                .limit(5)
                .collect(Collectors.toList());

        System.out.println("startCandidates: " + startCandidates);

        List<String> endCandidates = busStopRepository.findNearbyStationIdsWithGeo(
                        endStop.getXPos(), endStop.getYPos(), 200.0).stream()
                .limit(5)
                .collect(Collectors.toList());

        System.out.println("endCandidates: " + endCandidates);

        Set<String> visitedRouteIds = new HashSet<>();

        for (String candidateStartId : startCandidates) {
            for (String candidateEndId : endCandidates) {
                List<RouteResultDTO> directResults = findTransferRoutes(candidateStartId, candidateEndId);
                for (RouteResultDTO dto : directResults) {
                    if (!visitedRouteIds.contains(dto.getRouteId())) {
                        result.add(dto);
                        visitedRouteIds.add(dto.getRouteId());
                    }
                }
            }
        }

        return result;
    }

    public List<RouteResultDTO> findTransferRoutes(String startBsId, String endBsId) {
        List<RouteResultDTO> transferResults = new ArrayList<>();

        // 후보 찾기 (중간 생략)
        List<String> midPointsA = routeStopLinkRepository.findReachableStopsFrom(startBsId);
        List<String> midPointsB = routeStopLinkRepository.findReachableStopsTo(endBsId);
        Set<String> transferPoints = new HashSet<>(midPointsA);
        transferPoints.retainAll(midPointsB);

        BusStop startStop = busStopRepository.findByBsId(startBsId).orElseThrow();
        BusStop endStop = busStopRepository.findByBsId(endBsId).orElseThrow();

        List<String> sortedTransferPoints = transferPoints.stream()
                .map(bsId -> {
                    BusStop stop = busStopRepository.findByBsId(bsId).orElse(null);
                    if (stop == null) return null;
                    double distance = distanceBetween(startStop, stop) + distanceBetween(stop, endStop);
                    return new TransferCandidate(bsId, distance);
                })
                .filter(Objects::nonNull)
                .sorted(Comparator.comparingDouble(TransferCandidate::getTotalDistance))
                .limit(1)
                .map(TransferCandidate::getBsId)
                .collect(Collectors.toList());

        for (String transferBsId : sortedTransferPoints) {
            List<RouteResultDTO> firstLegs = findDirectRoutes(startBsId, transferBsId);
            List<RouteResultDTO> secondLegs = findDirectRoutes(transferBsId, endBsId);

            RouteResultDTO first = firstLegs.stream()
                    .min(Comparator.comparingInt(r -> r.getStationIds().size()))
                    .orElse(null);
            RouteResultDTO second = secondLegs.stream()
                    .min(Comparator.comparingInt(r -> r.getStationIds().size()))
                    .orElse(null);

            if (first == null || second == null) continue;
            if (first.getRouteId().equals(second.getRouteId())) continue;

            int totalStops = first.getStationIds().size() + second.getStationIds().size() - 1;
            if (totalStops > 30) continue;

            BusStop transferStop = busStopRepository.findByBsId(transferBsId).orElse(null);
            if (transferStop == null) continue;

            double direct = distanceBetween(startStop, endStop);
            double viaTransfer = distanceBetween(startStop, transferStop) + distanceBetween(transferStop, endStop);
            if (viaTransfer > direct * 2.5) continue;

            // 예상 소요 시간 계산
            double avgMinutesPerStop = 2.5; // 평균 정류장 간 시간 (분)
            double estimatedMinutes = totalStops * avgMinutesPerStop;

            List<BusStopDTO> fullPath = new ArrayList<>(first.getStationIds());
            if (second.getStationIds().size() > 1) {
                fullPath.addAll(second.getStationIds().subList(1, second.getStationIds().size()));
            }

            List<CoordinateDTO> orsPath = Collections.emptyList();

            RouteResultDTO dto = RouteResultDTO.builder()
                    .type("환승")
                    .routeId(first.getRouteId() + " → " + second.getRouteId())
                    .routeNo(first.getRouteNo() + " → " + second.getRouteNo())
                    .startBsId(startBsId)
                    .endBsId(endBsId)
                    .transferCount(1)
                    .stationIds(fullPath)
                    .transferStationId(transferBsId)
                    .transferStationName(transferStop.getBsNm() != null ? transferStop.getBsNm() : "알 수 없음")
                    .estimatedMinutes(estimatedMinutes)
                    .orsPath(orsPath) // 빈 리스트 전달
                    .build();

            transferResults.add(dto);
        }

        // 정렬 기준을 estimatedMinutes 로 변경
        transferResults.sort(Comparator.comparingDouble(RouteResultDTO::getEstimatedMinutes));

        return transferResults;
    }

    private double distanceBetween(BusStop a, BusStop b) {
        double dx = a.getXPos() - b.getXPos();
        double dy = a.getYPos() - b.getYPos();
        return Math.sqrt(dx * dx + dy * dy); // 유클리디안 거리
    }

    public List<RouteResultDTO> findRoutesNearCoords(double startX, double startY,
                                                     double endX, double endY,
                                                     double radius) {
        List<BusStop> startStops = busStopRepository.findStopsWithinRadius(startX, startY, radius);
        List<BusStop> endStops = busStopRepository.findStopsWithinRadius(endX, endY, radius);

        List<RouteResultDTO> result = new ArrayList<>();

        for (BusStop start : startStops) {
            for (BusStop end : endStops) {
                try {
                    List<RouteResultDTO> candidateRoutes = routeFinder.findRoutes(start.getBsId(), end.getBsId());

                    if (!candidateRoutes.isEmpty()) {
                        result.addAll(candidateRoutes);
                    }
                } catch (Exception e) {
                    // 경로가 없는 경우는 무시 (예외 삼키기 or 로그 남기기)
                }
            }
        }

        return result;
    }

}

