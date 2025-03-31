package kroryi.bus2.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import kroryi.bus2.dto.ODsayDataDTO.PolylinePointDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

@Service
@Log4j2
@RequiredArgsConstructor
public class RouteDataOdsayService {

    @Value("${odsay.api-key}")
    private String apiKey;

    private final RestTemplate restTemplate;

    public String getOdsayBusId(String routeNo, String routeNote) throws IOException {
        String url = "https://api.odsay.com/v1/api/searchBusLane"
                + "?lang=0"
                + "&busNo=" + routeNo
                + "&cityCode=4000"
                + "&apiKey=" + apiKey;

        System.out.println("🔍 요청 URL: " + url);

        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new IOException("ODsay API 요청 실패");
        }

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(response.getBody());

        JsonNode busLaneList = jsonNode.path("result").path("lane");
        if (busLaneList.isMissingNode() || !busLaneList.isArray() || busLaneList.size() == 0) {
            throw new IOException("ODsay 노선 검색 결과 없음");
        }

        Set<String> allowedCities = Set.of(
                "대구", "경산", "영천", "청도군", "고령군", "성주군", "김천", "칠곡군"
        );

        List<String> userStops = extractStopsFromUserInput(routeNote);
        System.out.println("🛠 사용자 방면 정류장: " + userStops);

        double bestScore = 0.0;
        String bestBusId = null;
        String bestBusNo = null;

        for (JsonNode lane : busLaneList) {
            String cityName = lane.path("busCityName").asText();
            if (!allowedCities.contains(cityName)) {
                System.out.println("⚠️ 제외 도시: " + cityName);
                continue;
            }

            String apiBusNo = lane.path("busNo").asText();
            String busNameOnly = extractBusName(apiBusNo);
            if (!busNameOnly.equals(routeNo)) {
                System.out.println("❌ 노선명 불일치: 입력=" + routeNo + ", 응답=" + busNameOnly);
                continue;
            }

            List<String> candidateStops = extractStopsFromBusNo(apiBusNo);
            double similarity = calculateSimilarity(userStops, candidateStops);

            System.out.println("🔎 비교 대상: " + apiBusNo + " | 정류장: " + candidateStops + " | 유사도: " + similarity);

            if (similarity > bestScore) {
                bestScore = similarity;
                bestBusId = lane.path("busID").asText();
                bestBusNo = apiBusNo;
            }
        }

        if (bestBusId != null) {
            System.out.println("✅ 최종 선택된 노선: " + bestBusNo + " | 유사도: " + bestScore + " | busID: " + bestBusId);
            return bestBusId;
        }

        throw new IOException("허용된 도시에서 일치하는 방면 노선을 찾을 수 없습니다.");
    }



    public List<PolylinePointDTO> getPolylinePointsByBusId(String busId) throws IOException, URISyntaxException {
        String mapObject = String.format("0:0@%s:1:0:-1", busId);
        String url = "https://api.odsay.com/v1/api/loadLane"
                + "?lang=0"
                + "&mapObject=" + URLEncoder.encode(mapObject, StandardCharsets.UTF_8)
                + "&apiKey=" + apiKey;
        System.out.println("url: " + url);

        URI uri = new URI(url);
        ResponseEntity<String> response = restTemplate.getForEntity(uri, String.class);

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(response.getBody());

        // 에러 응답 체크
        if (root.has("error")) {
            String code = root.path("error").get(0).path("code").asText();
            String message = root.path("error").get(0).path("message").asText();
            throw new IOException("ODsay 에러 응답: " + code + " - " + message);
        }

        List<PolylinePointDTO> result = new ArrayList<>();

        JsonNode laneArray = root.path("result").path("lane");
        for (JsonNode lane : laneArray) {
            for (JsonNode section : lane.path("section")) {
                for (JsonNode pos : section.path("graphPos")) {
                    double x = pos.path("x").asDouble();
                    double y = pos.path("y").asDouble();
                    result.add(new PolylinePointDTO(x, y));
                }
            }
        }

        return result;
    }



    private String extractBusName(String fullName) {
        int idx = fullName.indexOf("(");
        return idx >= 0 ? fullName.substring(0, idx) : fullName;
    }

    private List<String> extractStopsFromUserInput(String routeNote) {
        return Arrays.stream(routeNote.split("->"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }

    private List<String> extractStopsFromBusNo(String busNo) {
        int start = busNo.indexOf('(');
        int end = busNo.indexOf(')');
        if (start < 0 || end <= start) return List.of();
        String inside = busNo.substring(start + 1, end);
        return Arrays.stream(inside.split("\\."))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }

    private double calculateSimilarity(List<String> user, List<String> candidate) {
        if (user.isEmpty() || candidate.isEmpty()) return 0.0;
        long match = user.stream().filter(candidate::contains).count();
        return (double) match / Math.max(user.size(), candidate.size());
    }

}