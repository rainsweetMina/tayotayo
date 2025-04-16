package kroryi.bus2.service.route;

import kroryi.bus2.dto.Route.RouteResultDTO;
import kroryi.bus2.dto.busStop.BusStopDTO;
import kroryi.bus2.dto.busStop.TransferCandidate;
import kroryi.bus2.entity.busStop.BusStop;
import kroryi.bus2.entity.route.Route;
import kroryi.bus2.repository.jpa.board.RouteStopLinkRepository;
import kroryi.bus2.repository.jpa.bus_stop.BusStopRepository;
import kroryi.bus2.repository.jpa.route.RouteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RouteFinderService {

    private final RouteRepository routeRepository;
    private final RouteStopLinkRepository routeStopLinkRepository;
    private final BusStopRepository busStopRepository;

    // 출,도착 정류소들의 특정 미터안의 후보를 찾는거
    public List<RouteResultDTO> findRoutesWithNearbyStart(String startBsId, String endBsId) {
        List<RouteResultDTO> result = new ArrayList<>();

        BusStop startStop = busStopRepository.findByBsId(startBsId)
                .orElseThrow(() -> new RuntimeException("출발 정류장을 찾을 수 없습니다: " + startBsId));

        BusStop endStop = busStopRepository.findByBsId(endBsId)
                .orElseThrow(() -> new RuntimeException("도착 정류장을 찾을 수 없습니다: " + endBsId));

        List<String> startCandidates = busStopRepository.findNearbyStationIdsWithGeo(
                startStop.getXPos(), startStop.getYPos(), 300.0);
        List<String> endCandidates = busStopRepository.findNearbyStationIdsWithGeo(
                endStop.getXPos(), endStop.getYPos(), 300.0);

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

            String moveDir = moveDirs.get(0); // 첫 번째 방향 사용

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

            // 정류장 수 기반 예상 시간 계산
            int estimatedMinutes = (int) Math.round(stationIds.size() * 2.5);

            result.add(RouteResultDTO.builder()
                    .type("직통")
                    .routeId(route.getRouteId())
                    .routeNo(route.getRouteNo())
                    .startBsId(startBsId)
                    .endBsId(endBsId)
                    .transferCount(0)
                    .stationIds(stationIds)
                    .estimatedMinutes(estimatedMinutes)
                    .build());
        }

        return result;
    }


    public List<RouteResultDTO> findRoutesWithNearbyStart2(String startBsId, String endBsId) {
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

            RouteResultDTO dto = RouteResultDTO.builder()
                    .type("환승")
                    .routeId(first.getRouteId() + " → " + second.getRouteId())
                    .routeNo(first.getRouteNo() + " → " + second.getRouteNo())
                    .startBsId(startBsId)
                    .endBsId(endBsId)
                    .transferCount(1)
                    .stationIds(fullPath)
                    .transferStationId(transferBsId)
                    .transferStationName(
                            transferStop.getBsNm() != null ? transferStop.getBsNm() : "알 수 없음")
                    .estimatedMinutes(estimatedMinutes)
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


//    public List<RouteResultDTO> findTransferRoutes(String startBsId, String endBsId) {
//        List<RouteResultDTO> transferResults = new ArrayList<>();
//
//        // 1. 후보 정류장 리스트
//        BusStop start = busStopRepository.findByBsId(startBsId).orElseThrow();
//        BusStop end = busStopRepository.findByBsId(endBsId).orElseThrow();
//
//        List<String> startCandidates = busStopRepository.findNearbyStationIdsWithGeo(start.getXPos(), start.getYPos(), 150.0);
//        List<String> endCandidates = busStopRepository.findNearbyStationIdsWithGeo(end.getXPos(), end.getYPos(), 150.0);
//
//        // 2. 출발 후보 → 환승 후보들
//        for (String sc : startCandidates) {
//            List<String> midPointsA = routeStopLinkRepository.findReachableStopsFrom(sc); // sc에서 도달 가능한 정류장들
//
//            for (String ec : endCandidates) {
//                List<String> midPointsB = routeStopLinkRepository.findReachableStopsTo(ec); // ec로 갈 수 있는 정류장들
//
//                // 3. 교집합: 환승 가능한 정류장 찾기
//                Set<String> transferPoints = new HashSet<>(midPointsA);
//                transferPoints.retainAll(midPointsB); // 둘 다 포함되는 환승 정류장
//
//                for (String transferBsId : transferPoints) {
//                    // 4. 두 구간으로 쪼개서 직통 경로 조회
//                    List<RouteResultDTO> firstLegs = findDirectRoutes(sc, transferBsId);
//                    List<RouteResultDTO> secondLegs = findDirectRoutes(transferBsId, ec);
//
//                    for (RouteResultDTO first : firstLegs) {
//                        for (RouteResultDTO second : secondLegs) {
//                            // 5. 통합 경로로 합치기
//                            List<BusStopDTO> fullPath = new ArrayList<>();
//                            fullPath.addAll(first.getStationIds());
//                            fullPath.addAll(second.getStationIds().subList(1, second.getStationIds().size())); // 환승지 중복 제거
//
//                            transferResults.add(RouteResultDTO.builder()
//                                    .type("환승")
//                                    .routeId(first.getRouteId() + " → " + second.getRouteId())
//                                    .routeNo(first.getRouteNo() + " → " + second.getRouteNo())
//                                    .startBsId(first.getStartBsId())
//                                    .endBsId(second.getEndBsId())
//                                    .transferCount(1)
//                                    .stationIds(fullPath)
//                                    .build());
//                        }
//                    }
//                }
//            }
//        }
//
//        return transferResults;
//    }

}

