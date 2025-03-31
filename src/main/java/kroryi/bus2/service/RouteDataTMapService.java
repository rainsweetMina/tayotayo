package kroryi.bus2.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import kroryi.bus2.dto.TMapDTO.LatLngDTO;
import kroryi.bus2.dto.coordinate.CoordinateDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Log4j2
@RequiredArgsConstructor
public class RouteDataTMapService {

    @Value("${tmap.api.key}")
    private String tmapApiKey;

    private final ObjectMapper objectMapper;
    private final RouteDataService routeDataService;
    private final RestTemplate restTemplate;


    public Map<String, List<LatLngDTO>> getTMapRouteByBusDirection(String routeId) throws IOException {
        JsonNode result = routeDataService.getBusRoute(routeId); // 노선 ID로 정류장 좌표 목록 조회
        System.out.println("result: " + result);
        JsonNode root = objectMapper.readTree(result.traverse());
        JsonNode items = root.get("body").get("items");

        List<LatLngDTO> forwardPoints = new ArrayList<>();
        List<LatLngDTO> reversePoints = new ArrayList<>();

        for (JsonNode item : items) {
            if (item.hasNonNull("xPos") && item.hasNonNull("yPos") && item.hasNonNull("moveDir")) {
                double x = Double.parseDouble(item.get("xPos").asText());
                double y = Double.parseDouble(item.get("yPos").asText());
                int moveDir = item.get("moveDir").asInt();

                LatLngDTO point = new LatLngDTO(y, x); // TMap은 lat, lng 기준
                if (moveDir == 0) reversePoints.add(point);
                else if (moveDir == 1) forwardPoints.add(point);
            }
        }

        forwardPoints = removeDuplicatePoints(forwardPoints);
        reversePoints = removeDuplicatePoints(reversePoints);

        Map<String, List<LatLngDTO>> resultMap = new HashMap<>();
        resultMap.put("reverse", getChunkedTMapRoute(reversePoints));
        resultMap.put("forward", getChunkedTMapRoute(forwardPoints));

        return resultMap;
    }

    private List<LatLngDTO> removeDuplicatePoints(List<LatLngDTO> original) {
        return original.stream()
                .distinct()
                .collect(Collectors.toList());
    }

    public List<LatLngDTO> getChunkedTMapRoute(List<LatLngDTO> stations) {
        List<LatLngDTO> fullRoute = new ArrayList<>();

        int maxChunk = 4;
        for (int i = 0; i < stations.size() - 1; i += (maxChunk - 1)) {
            int endIdx = Math.min(i + maxChunk, stations.size());
            List<LatLngDTO> chunk = stations.subList(i, endIdx);

            // 디버깅: 요청 chunk 좌표 로그
            log.info("🧩 [TMap 요청 chunk] {} ~ {} : {}", i, endIdx - 1, chunk);

            // 좌표 유효성 검사
            for (LatLngDTO p : chunk) {
                if (Double.isNaN(p.getLat()) || Double.isNaN(p.getLng()) || p.getLat() == 0.0 || p.getLng() == 0.0) {
                    log.warn("⚠️ 유효하지 않은 좌표 발견: {}", p);
                }
            }

            try {
                List<LatLngDTO> segment = getPolylineByStations(chunk);
                System.out.println("요청 성공");
                fullRoute.addAll(segment);
            } catch (Exception e) {
                log.warn("TMap 경로 요청 실패: chunk size = {}", chunk.size(), e);
            }
        }

        return fullRoute;
    }

    public List<LatLngDTO> getPolylineByStations(List<LatLngDTO> stationCoords) {
        if (stationCoords.size() < 2) {
            throw new IllegalArgumentException("정류장은 최소 2개 이상이어야 합니다.");
        }

        LatLngDTO start = stationCoords.get(0);
        LatLngDTO end = stationCoords.get(stationCoords.size() - 1);

        // 중간 경유지 문자열 생성
        String passList = stationCoords.subList(1, stationCoords.size() - 1).stream()
                .map(c -> String.format("%f,%f", c.getLng(), c.getLat()))
                .collect(Collectors.joining("_"));

        // 요청 본문 구성
        Map<String, Object> body = new HashMap<>();
        body.put("startX", start.getLng());
        body.put("startY", start.getLat());
        body.put("endX", end.getLng());
        body.put("endY", end.getLat());
        body.put("reqCoordType", "WGS84GEO");
        body.put("resCoordType", "WGS84GEO");
        body.put("searchOption", "0"); // 추천경로

        if (!passList.isEmpty()) {
            body.put("passList", passList);
        }

        // 디버깅: 요청 바디 확인
        try {
            log.debug("📤 [TMap 요청 바디] {}", objectMapper.writeValueAsString(body));
        } catch (Exception e) {
            log.warn("❗ 요청 바디 로깅 실패", e);
        }

        // 헤더 구성
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("appKey", tmapApiKey);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        String url = "https://apis.openapi.sk.com/tmap/routes?version=1&format=json";

        ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

        // 디버깅: 응답 상태 확인
        if (!response.getStatusCode().is2xxSuccessful()) {
            log.warn("📛 TMap 응답 실패: {}", response.getStatusCode());
        }

        // 응답 파싱
        List<LatLngDTO> polyline = new ArrayList<>();
        List<Map<String, Object>> features = (List<Map<String, Object>>) response.getBody().get("features");

        for (Map<String, Object> feature : features) {
            Map<String, Object> geometry = (Map<String, Object>) feature.get("geometry");
            if ("LineString".equals(geometry.get("type"))) {
                List<List<Double>> coords = (List<List<Double>>) geometry.get("coordinates");

                // ✅ 응답이 너무 짧으면 (직선 가능성 높음)
                if (coords.size() <= 2) {
                    log.warn("⚠️ 직선 경로 응답 감지 → 제외: {} → {}", start, end);
                    continue; // 이 구간은 제외
                }

                for (List<Double> coord : coords) {
                    polyline.add(new LatLngDTO(coord.get(1), coord.get(0))); // [lat, lng]
                }
            }
        }

        return polyline;
    }
}



