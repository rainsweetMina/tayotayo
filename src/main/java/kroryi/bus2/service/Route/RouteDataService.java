package kroryi.bus2.service.Route;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import kroryi.bus2.dto.Route.RouteDTO;
import kroryi.bus2.dto.busStop.XyPointDTO;
import kroryi.bus2.dto.coordinate.CoordinateDTO;

import kroryi.bus2.entity.CustomRoute;
import kroryi.bus2.entity.Route;

import kroryi.bus2.repository.jpa.NodeRepository;
import kroryi.bus2.repository.jpa.route.CustomRouteRepository;
import kroryi.bus2.repository.jpa.route.RouteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Log4j2
@RequiredArgsConstructor
// 버스 노선에 관한 서비스 클래스.
public class RouteDataService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final RouteRepository routeRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final NodeRepository nodeRepository;
    private final CustomRouteRepository customRouteRepository;


    @Value("${api.bus.base-url}")
    private String baseUrl;

    @Value("${api.service-key-encoding}")
    private String encoding_serviceKey;

    @Value("${api.service-key-decoding}")
    private String decoding_serviceKey;

    @Value("${ors.api.key}")
    private String orsApiKey;

    // 버스 노선명으로 검색 (일반 노선)
    public List<Route> getBusByNm(String routeNo) {
        System.out.println("[DB 조회] 일반 노선 - routeNo: " + routeNo);
        List<Route> result = routeRepository.searchByRouteNumberFull(routeNo);
        System.out.printf("노선 result: %s\n", result);
        return result;
    }

    public List<CustomRoute> getCustomBusByNm(String keyword) {
        return customRouteRepository.searchByRouteNumberFull(keyword);
    }



        // 노선ID로 경로 가져옴
    public JsonNode getBusRoute(String routeId) throws IOException {
        String redisKey = "BUS_ROUTE::" + routeId;

        // ✅ Redis에 JSON 문자열로 저장하고, 꺼낼 때 다시 JsonNode로 파싱
        String cachedJson = (String) redisTemplate.opsForValue().get(redisKey);
        if (cachedJson != null) {
            System.out.println("✅ Redis에서 버스 경로 정보 캐시 불러옴: " + redisKey);
            ObjectMapper objectMapper = new ObjectMapper(); // ✅ JSON 파서로 변경
            return objectMapper.readTree(cachedJson);
        }

        System.out.println("📍 API 요청 routeId: " + routeId);

        URI uri = UriComponentsBuilder.fromHttpUrl(baseUrl + "/getBs")
                .queryParam("serviceKey", encoding_serviceKey)
                .queryParam("routeId", routeId)
                .build(true)
                .toUri();

        System.out.println("[버스 경로 최종 요청 URI] " + uri);

        String response = restTemplate.getForObject(uri, String.class);

        XmlMapper xmlMapper = new XmlMapper();
        JsonNode jsonNode = xmlMapper.readTree(response.getBytes());

        System.out.println("[XML → JSON] 변환 결과: " + jsonNode);

        // ✅ Redis에 문자열(JSON 형태)로 저장
        redisTemplate.opsForValue().set(redisKey, jsonNode.toString(), Duration.ofMinutes(10));
        System.out.println("📝 Redis에 버스 경로 정보 캐싱 완료: " + redisKey);

        return jsonNode;
    }





    /**
     * ORS(OpenRouteService)를 이용해 버스 노선의 정방향 / 역방향 경로 좌표를 계산하여 반환
     * @param routeId 버스 노선 ID
     * @return 정방향(forward), 역방향(reverse) 좌표 리스트 Map
     */
    public Map<String, List<CoordinateDTO>> getOrsRouteByBusDirection(String routeId) throws IOException, InterruptedException {
        JsonNode result = getBusRoute(routeId);    // 노선ID로 버스 정류장 노선 불러옴
        JsonNode root = objectMapper.readTree(result.traverse());
        JsonNode items = root.get("body").get("items");

        List<XyPointDTO> points0 = new ArrayList<>();
        List<XyPointDTO> points1 = new ArrayList<>();

        for (JsonNode item : items) {
            if (item.hasNonNull("xPos") && item.hasNonNull("yPos") && item.hasNonNull("moveDir")) {
                double x = Double.parseDouble(item.get("xPos").asText());
                double y = Double.parseDouble(item.get("yPos").asText());
                int moveDir = item.get("moveDir").asInt();

                XyPointDTO point = new XyPointDTO(x, y, moveDir);
                if (moveDir == 0) points0.add(point);
                else if (moveDir == 1) points1.add(point);
            }
        }

        Map<String, List<CoordinateDTO>> resultMap = new HashMap<>();
        resultMap.put("reverse", getChunkedOrs(points0));
        resultMap.put("forward", getChunkedOrs(points1));
        return resultMap;
    }
    private static final double MAX_DISTANCE_THRESHOLD = 500; // 미터 단위, 적절히 조정하세요.

    // 출력결과가 70개로 한정된 쪼잔한 api인 ORS를 뚫기 위해 탄생한 역작, 69개씩 잘라서 출력 시켜서 합쳐줌
    private List<CoordinateDTO> getChunkedOrs(List<XyPointDTO> points) throws IOException, InterruptedException {
        List<CoordinateDTO> coordinates = points.stream()
                .map(p -> new CoordinateDTO(p.getXPos(), p.getYPos()))
                .collect(Collectors.toList());

        List<CoordinateDTO> result = new ArrayList<>();
        for (int i = 0; i < coordinates.size() - 1; i += 69) {
            int toIndex = Math.min(i + 70, coordinates.size());
            List<CoordinateDTO> chunk = coordinates.subList(i, toIndex);

            try {
                result.addAll(getOrsPath(chunk));
            } catch (IOException e) {
                log.warn("🚫 ORS 요청 실패 → fallback으로 직선 연결: {}", chunk);
                result.addAll(chunk); // 🔁 그냥 직선 연결
            }
        }


        return result;
    }

    // 정방향과 역방향으로 구분된 노선들의 정류소 좌표를 ORS에 넣어서 노선도 좌표들을 반환
    public List<CoordinateDTO> getOrsPath(List<CoordinateDTO> coordinates) throws IOException, InterruptedException {
        String url = "https://api.openrouteservice.org/v2/directions/driving-car";

        // ORS 요청용 좌표 구성: [ [x, y], [x, y], ... ]
        List<List<Double>> orsCoordinates = coordinates.stream()


                .map(c -> Arrays.asList(c.getXPos(), c.getYPos()))
                .collect(Collectors.toList());

        Map<String, Object> body = new HashMap<>();
        body.put("coordinates", orsCoordinates);

        // HTTP 요청 생성
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", orsApiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)))
                .build();

        // 요청 실행
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
//        log.warn("ORS 응답 바디: {}", response.body());

        if (response.statusCode() != 200) {
            throw new IOException("OpenRouteService 요청 실패: " + response.body());
        }

        JsonNode root = objectMapper.readTree(response.body());
        String encodedPolyline = root.get("routes").get(0).get("geometry").asText();
        System.out.printf("encodedPolyline: %s\n", encodedPolyline);
        return decodePolyline(encodedPolyline);
    }

    // ORS의 encoded polyline 문자열을 경도/위도 좌표로 복원
    // 경로 암호(polyline)를 해독해 실제 경로를 복원하는 디코더 ( ORS )
    public static List<CoordinateDTO> decodePolyline(String encoded) {
        List<CoordinateDTO> coordinates = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int deltaLat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += deltaLat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int deltaLng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += deltaLng;

            double latitude = lat / 1e5;
            double longitude = lng / 1e5;
            coordinates.add(new CoordinateDTO(longitude, latitude));
        }
        return coordinates;
    }

    // 흐름 요약
// getOrsRouteByBusDirection()          // 노선 ID로 정방향/역방향 정류장 분류 및 ORS 호출
// └── getChunkedOrs()                  // ORS 요청 좌표를 70개 미만씩 잘라서 순차 요청
//       └── getOrsPath()               // 실제 ORS API에 HTTP 요청하여 polyline 응답 받음
//             └── decodePolyline()     // ORS의 polyline 인코딩 문자열을 좌표 리스트로 변환
//
// 진짜진짜 요약 : 노선ID로 노선불러와서 정방향, 역방향 구분 후 각각 ORS에 넣어 버스 노선도의 좌표를 받아서 인코딩 후 합쳐서 반환


    // 그냥노선 + Custom노선의 정보를 가져오는거
    public RouteDTO getRouteByRouteId(String routeId) {
        return routeRepository.findByRouteId(routeId)
                .map(this::convertToDTO)
                .or(() -> customRouteRepository.findByRouteId(routeId).map(this::convertToDTO))
                .orElseThrow(() -> new IllegalArgumentException("노선이 존재하지 않습니다: " + routeId));
    }

    private RouteDTO convertToDTO(Route route) {
        return RouteDTO.builder()
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
                .build();
    }

    private RouteDTO convertToDTO(CustomRoute route) {
        return RouteDTO.builder()
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
                .build();
    }


}
