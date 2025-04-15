package kroryi.bus2.service.board;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import kroryi.bus2.dto.busStop.BusStopDTO;
import kroryi.bus2.entity.route.RouteStopLink;
import kroryi.bus2.repository.jpa.board.RouteStopLinkRepository;
import kroryi.bus2.repository.jpa.bus_stop.BusStopRepository;
import kroryi.bus2.repository.jpa.LinkRepository;
import kroryi.bus2.repository.jpa.NodeRepository;
import kroryi.bus2.repository.jpa.route.RouteRepository;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Log4j2
@ToString
public class RouteStopLinkService {
    private final NodeRepository nodeRepository;
    private final BusStopRepository busStopRepository;
    private final RouteRepository routeRepository;
    private final LinkRepository linkRepository;
    private final RouteStopLinkRepository routeStopLinkRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${public.api-key}")
    private String serviceKey;

    // 해당 버스 전체 노선 조회
    public List<BusStopDTO> getStopsWithNamesByRouteId(String routeId) {
        List<Object[]> rows = routeStopLinkRepository.findRawStopDataByRouteId(routeId);

        return rows.stream()
                .map(row -> new BusStopDTO(
                        ((Number) row[0]).intValue(),      // seq
                        (String) row[1],                   // bsNm
                        (String) row[2],                   // bsId
                        (Double) row[3],                   // xPos
                        (Double) row[4]                    // yPos
                ))
                .collect(Collectors.toList());
    }

    public List<BusStopDTO> getStopsWithNamesByRouteIdAndMoveDir(String routeId, String moveDir) {
        List<Object[]> rows = routeStopLinkRepository.findRawStopDataByRouteIdAndMoveDir(routeId, moveDir);

        return rows.stream()
                .map(row -> new BusStopDTO(
                        ((Number) row[0]).intValue(),      // seq
                        (String) row[1],                   // bsNm
                        (String) row[2],                   // bsId
                        (Double) row[3],                   // xPos
                        (Double) row[4]                    // yPos
                ))
                .collect(Collectors.toList());
    }

    // 특정 노선 정보 가져오기
    public void fetchSingleRouteStopLink(String routeId) {
        try {
            String apiUrl = "https://apis.data.go.kr/6270000/dbmsapi01/getBs?" +
                    "serviceKey=" + URLEncoder.encode(serviceKey, StandardCharsets.UTF_8) +
                    "&routeId=" + URLEncoder.encode(routeId, StandardCharsets.UTF_8) +
                    "&_type=json";

            fetchAndSaveBusData(apiUrl, routeId);
        } catch (Exception e) {
            log.error("단일 routeId [{}] 처리 중 오류: {}", routeId, e.getMessage());
        }
    }
    
    public void fetchAndSaveBusData(String apiUrl, String routeId) {
        try {
            URI uri = new URI(apiUrl);
            String response = restTemplate.getForObject(uri, String.class);

            XmlMapper xmlMapper = new XmlMapper();
            JsonNode node = xmlMapper.readTree(response.getBytes());

            ObjectMapper jsonMapper = new ObjectMapper();
            JsonNode jsonNode = jsonMapper.readTree(jsonMapper.writeValueAsString(node));
            System.out.println("변환된 JSON 데이터: " + jsonNode.toPrettyString());

            JsonNode itemNode = jsonNode.path("body").path("items");

            if (itemNode.isArray()) {
                saveRouteStopLinks(itemNode, routeId);
            } else if (!itemNode.isMissingNode() && itemNode.size() != 0) {
                ArrayNode singleNodeArray = objectMapper.createArrayNode();
                singleNodeArray.add(itemNode);
                saveRouteStopLinks(singleNodeArray, routeId);
            } else {
                System.out.println("routeId [" + routeId + "]의 데이터가 없습니다.");
            }
        } catch (Exception e) {
            System.out.println("routeId [" + routeId + "] 처리 중 예외 발생: " + e.getMessage());
        }
    }
    
    private void saveRouteStopLinks(JsonNode nodeArray, String routeId) {

        for (JsonNode node : nodeArray) {
            try {
                RouteStopLink link = new RouteStopLink();
                link.setRouteId(routeId); // 파라미터로 받은 노선 ID
                link.setBsId(node.path("bsId").asText()); // 정류장 ID
                link.setSeq(node.path("seq").asInt()); // 순차적으로 증가
                link.setMoveDir(node.path("moveDir").asText()); // 이동방향
                link.setXPos(node.path("xPos").asDouble());
                link.setYPos(node.path("yPos").asDouble());

                System.out.println("✅ 저장 중: " + link);
                routeStopLinkRepository.save(link);
                System.out.println("✅ 저장 완료");
            } catch (Exception e) {
                System.out.println("저장 실패: " + e.getMessage());
            }
        }
    }

    // 전체 노선 가져오기
    public void fetchAndSaveAllRouteStopLinks() {
        List<String> routeIds = routeRepository.findAllRouteIds(); // routeId 리스트 가져오기

        for (String routeId : routeIds) {
            try {
                String apiUrl = "https://apis.data.go.kr/6270000/dbmsapi01/getBs?" +
                        "serviceKey=" + URLEncoder.encode(serviceKey, StandardCharsets.UTF_8) +
                        "&routeId=" + URLEncoder.encode(routeId, StandardCharsets.UTF_8) +
                        "&_type=json";

                fetchAndSaveAllBusData(apiUrl, routeId);
            } catch (Exception e) {
                System.out.println("routeId [" + routeId + "] 처리 중 오류: " + e.getMessage());
            }
        }
    }

    public void fetchAndSaveAllBusData(String apiUrl, String routeId) {
        try {
            URI uri = new URI(apiUrl);
            String response = restTemplate.getForObject(uri, String.class);

            XmlMapper xmlMapper = new XmlMapper();
            JsonNode node = xmlMapper.readTree(response.getBytes());

            ObjectMapper jsonMapper = new ObjectMapper();
            JsonNode jsonNode = jsonMapper.readTree(jsonMapper.writeValueAsString(node));
            System.out.println("변환된 JSON 데이터: " + jsonNode.toPrettyString());

            JsonNode itemNode = jsonNode.path("body").path("items");

            if (itemNode.isArray()) {
                saveRouteStopLinks(itemNode, routeId);
            } else if (!itemNode.isMissingNode() && !itemNode.isNull()) {
                ArrayNode singleNodeArray = objectMapper.createArrayNode();
                singleNodeArray.add(itemNode);
                saveRouteStopLinksFilter(singleNodeArray, routeId);
            } else {
                System.out.println("❌ routeId [" + routeId + "]의 데이터가 없습니다.");
            }
        } catch (Exception e) {
            System.out.println("❌ routeId [" + routeId + "] 처리 중 예외 발생: " + e.getMessage());
        }
    }

    private void saveRouteStopLinksFilter(JsonNode nodeArray, String routeId) {
        for (JsonNode node : nodeArray) {
            try {
                int seq = node.path("seq").asInt();

                // 중복 검사: routeId + seq 기준
                boolean exists = routeStopLinkRepository.existsByRouteIdAndSeq(routeId, seq);
                if (exists) {
                    System.out.println("⚠️ 중복 건너뜀: routeId=" + routeId + ", seq=" + seq);
                    continue;
                }

                RouteStopLink link = new RouteStopLink();
                link.setRouteId(routeId);
                link.setBsId(node.path("bsId").asText());
                link.setSeq(seq);
                link.setMoveDir(node.path("moveDir").asText());
                link.setXPos(node.path("xPos").asDouble());
                link.setYPos(node.path("yPos").asDouble());

                System.out.println("✅ 저장 중: " + link);
                routeStopLinkRepository.save(link);
                System.out.println("✅ 저장 완료");
            } catch (Exception e) {
                System.out.println("❌ 저장 실패: " + e.getMessage());
            }
        }
    }



}
